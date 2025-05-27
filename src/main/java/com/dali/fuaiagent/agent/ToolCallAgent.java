package com.dali.fuaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.dali.fuaiagent.agent.model.AgentState;
// import com.dali.fuaiagent.agent.ThinkResult; // 如果 ThinkResult 是独立文件
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    private final ToolCallback[] availableTools;
    private ChatResponse toolCallChatResponse;
    private final ToolCallingManager toolCallingManager;
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    @Override
    public ThinkResult think() { // 修改返回类型
        List<Message> messagesForThisTurn = new ArrayList<>(getMessageList());

        // 卡顿检测与处理逻辑 (与上一版本相同)
        if (getCurrentStep() > 1 &&
                toolCallChatResponse != null &&
                toolCallChatResponse.getResult() != null &&
                toolCallChatResponse.getResult().getOutput() != null &&
                toolCallChatResponse.getResult().getOutput().getToolCalls().isEmpty()) {
            Message lastMessageInHistory = getMessageList().get(getMessageList().size() - 1);
            if (lastMessageInHistory instanceof AssistantMessage) {
                String lastAssistantContent = lastMessageInHistory.getText(); // 使用 getContent()
                if (lastAssistantContent != null &&
                        (lastAssistantContent.contains("？") ||
                                lastAssistantContent.contains("?")  ||
                                lastAssistantContent.toLowerCase().contains("what can i do") ||
                                lastAssistantContent.toLowerCase().contains("我可以帮助") ||
                                lastAssistantContent.contains("请告诉我") ||
                                lastAssistantContent.contains("需要什么帮助"))) {
                    String stallBreakingUserMessage = """
                    你正处在一个多步骤执行流程中。在上一个步骤里，你没有选择任何工具，并且似乎在等待用户提供更多信息或具体任务。
                    用户在当前步骤没有提供新的输入。
                    请严格回顾你可用的工具以及基于对话历史的总体目标，特别是你的操作指南中关于在无法继续时必须终止的指令。
                    你的工具中有一个叫做 'doTerminate'，它的描述是：“当请求得到满足，或者当助手无法进一步处理任务时，终止交互。当你完成了所有任务后，调用此工具结束工作。”
                    如果你判断当前无法基于已有信息进一步处理任务，或者没有明确的任务可以执行，你现在必须使用 'doTerminate' 工具。
                    如果任务不明确，不要再次询问任务是什么。请选择一个行动或坚决终止。
                    """;
                    messagesForThisTurn.add(new UserMessage(stallBreakingUserMessage));
                    log.info("AGENT_DEBUG: 为智能体 '{}' 在步骤 {} 注入了“卡顿打破”用户消息。", getName(), getCurrentStep());
                }
            }
        }

        Prompt prompt = new Prompt(messagesForThisTurn, chatOptions);

        try {
            ChatResponse response = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();

            this.setToolCallChatResponse(response); // 存储完整的ChatResponse
            AssistantMessage assistantMessage = response.getResult().getOutput();
            getMessageList().add(assistantMessage);

            String assistantTextOutput = assistantMessage.getText(); // 获取AI的文本输出
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();

            log.info(getName() + "的思考: " + (assistantTextOutput == null || assistantTextOutput.isBlank() ? "[无文本思考]" : assistantTextOutput));
            log.info(getName() + "选择了 " + toolCalls.size() + " 个工具调用");

            if (!toolCalls.isEmpty()) {
                String toolCallInfo = toolCalls.stream()
                        .map(toolCall -> String.format("工具名称: %s, 参数: %s", toolCall.name(), toolCall.arguments()))
                        .collect(Collectors.joining("\n"));
                log.info(toolCallInfo);
                // 即使调用工具，assistantTextOutput 也可能是AI决定调用工具前的思考文本，可以一并返回
                return new ThinkResult(true, assistantTextOutput);
            } else {
                // 没有工具调用，返回AI的文本回复
                return new ThinkResult(false, assistantTextOutput);
            }
        } catch (Exception e) {
            log.error("智能体 " + getName() + " 在步骤 " + getCurrentStep() + " 的思考阶段出错", e);
            return new ThinkResult(false, "思考阶段发生错误: " + e.getMessage());
        }
    }

    // act() 方法保持不变，它返回工具执行的摘要
    @Override
    public String act() {
        if (this.toolCallChatResponse == null || !this.toolCallChatResponse.hasToolCalls()) {
            log.warn("智能体 {} 调用了 act() 但在存储的 toolCallChatResponse 中未找到工具调用。这可能表示 think() 中存在逻辑缺陷。", getName());
            return "没有可执行的工具调用，尽管 act() 被调用了。";
        }

        Prompt promptForToolExecution = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(promptForToolExecution, this.toolCallChatResponse);
        setMessageList(toolExecutionResult.conversationHistory());

        List<ToolResponseMessage> toolResponseMessages = toolExecutionResult.conversationHistory().stream()
                .filter(message -> message instanceof ToolResponseMessage)
                .map(message -> (ToolResponseMessage) message)
                .collect(Collectors.toList());

        if (toolResponseMessages.isEmpty()) {
            log.warn("智能体 {} 在工具执行后，历史记录中未找到 ToolResponseMessage。", getName());
            return "工具执行完成，但在历史记录中未找到工具响应。";
        }

        ToolResponseMessage lastToolResponsesBatch = CollUtil.getLast(toolResponseMessages);
        if (lastToolResponsesBatch == null) {
            return "工具执行未产生可处理的工具响应。";
        }

        String resultsSummary = lastToolResponsesBatch.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成了它的任务！结果: " + response.responseData())
                .collect(Collectors.joining("\n"));

        boolean terminateToolCalled = lastToolResponsesBatch.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));

        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
            log.info("智能体 {} 调用了终止工具。智能体状态已设置为 FINISHED。", getName());
        }

        log.info("智能体 {} 的工具执行结果:\n{}", getName(), resultsSummary);
        return resultsSummary;
    }
}