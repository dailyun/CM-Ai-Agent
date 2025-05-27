package com.dali.fuaiagent.agent;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgentOptions;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.dali.fuaiagent.agent.model.AgentState;
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

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{

    private final ToolCallback[] availableTools;

    private ChatResponse toolCallChatResponse;

    private final ToolCallingManager toolCallingManager;

    private final ChatOptions  chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                        .withProxyToolCalls(true)
                        .build();
    }



    @Override
    public boolean think() {

        if (getNextStepPrompt() != null &&getNextStepPrompt().isEmpty() ) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);

        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            //  记录响应， 用于 Act
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();

            // 输出提示信息
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            log.info(getName() + "的思考" +result);
            log.info(getName() + "选择了" + toolCalls.size() + "个工具调用");
            String toolCallInfo = toolCalls.stream()
                    .map(toolCall -> String.format("工具名称: %s, 参数: %s",toolCall.name(),toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if (toolCalls.isEmpty()) {
                getMessageList().add(assistantMessage);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error("Error in think", e);
            return false;
        }

    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "No tool calls";
        }
        // 获取工具调用
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        //  记录响应
        setMessageList(toolExecutionResult.conversationHistory());
        //  返回结果
        // 当前工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成了它的任务！结果: " + response.responseData())
                .collect(Collectors.joining("\n"));
// 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(results);
        return results;

    }
}
