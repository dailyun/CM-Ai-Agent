package com.dali.fuaiagent.agent;

import com.dali.fuaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil; // 你原来使用的 StringUtil
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException; // SseEmitter.send() 会抛出 IOException
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
public abstract class BaseAgent {

    private String name;
    private String systemPrompt;
    private String nextStepPrompt; // 尽管主要逻辑在 FuManus 的 systemPrompt，但基类保留此字段
    private AgentState state = AgentState.IDLE;
    private int maxSteps;
    private int currentStep; // 当前执行到的步骤号
    private ChatClient chatClient;
    private List<Message> messageList = new ArrayList<>();

    // 同步运行方法
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Agent " + name + " is not in IDLE state, current state: " + this.state);
        }
        if (StringUtil.isBlank(userPrompt)) { // 假设 StringUtil.isBlank 行为符合预期
            throw new RuntimeException("User message cannot be blank for agent " + name);
        }

        state = AgentState.RUNNING;
        messageList.add(new UserMessage(userPrompt));
        List<String> results = new ArrayList<>();

        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                currentStep = i + 1; // 更新当前步骤
                log.info("Agent {} - Executing step {}/{}", name, currentStep, maxSteps);
                String stepResult = step(); // 调用子类的 step 实现 (ReActAgent -> ToolCallAgent)

                results.add(stepResult);
                log.debug("Agent {} - Step {} result: {}", name, currentStep, stepResult);
            }

            if (state != AgentState.FINISHED) { // 如果循环结束不是因为 FINISHED 状态 (例如, 达到了 maxSteps)
                if (currentStep >= maxSteps) {
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    log.info("Agent {} - Terminated: Reached max steps ({})", name, maxSteps);
                }
                state = AgentState.FINISHED; // 确保最终状态是 FINISHED
            } else {
                results.add("Terminated: Agent finished its task.");
                log.info("Agent {} - Terminated: Agent explicitly finished its task.", name);
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error running agent " + name, e);
            return "Error running agent " + name + ": " + e.getMessage();
        } finally {
            this.cleanup(); // 清理资源
        }
    }

    // 抽象方法，由子类实现具体的步骤逻辑
    public abstract String step();

    // 清理方法，可由子类覆盖
    public void cleanup() {
        log.debug("Agent {} cleanup called. Current state: {}", name, state);
        // 可以在这里添加重置 messageList 或其他状态的逻辑，如果希望 agent 可以被复用
        // messageList.clear();
        // state = AgentState.IDLE; // 如果希望 agent 执行完一次后能直接再次使用
    }

    /**
     * 运行代理（流式输出）
     *
     * @param userPrompt 用户提示词
     * @return SseEmitter实例
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        final String agentName = getName(); // 在异步块中使用final局部变量

        // 定义SSE事件名称
        final String updateEventName = "agent_update";
        final String errorEventName = "agent_error";
        final String completionEventName = "agent_completion"; // 新增一个完成事件

        CompletableFuture.runAsync(() -> {
            log.info("Agent {} - Stream run started with user prompt: '{}'", agentName, userPrompt);
            try {
                if (this.state != AgentState.IDLE) {
                    sendSseEvent(emitter, errorEventName, "错误：智能体 " + agentName + " 当前状态为 " + this.state + "，无法运行。");
                    emitter.complete();
                    return;
                }
                if (StringUtil.isBlank(userPrompt)) {
                    sendSseEvent(emitter, errorEventName, "错误：智能体 " + agentName + " 的用户提示词不能为空。");
                    emitter.complete();
                    return;
                }

                this.state = AgentState.RUNNING;
                this.messageList.add(new UserMessage(userPrompt));

                try {
                    for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i++) {
                        this.currentStep = i + 1;
                        log.info("Agent {} - Stream executing step {}/{}", agentName, this.currentStep, maxSteps);

                        String stepResult = step(); // 调用核心逻辑
                        String resultPayload = "Step " + this.currentStep + ": " + stepResult;
                        sendSseEvent(emitter, updateEventName, resultPayload);
                        log.debug("Agent {} - Stream step {} result sent: {}", agentName, this.currentStep, stepResult);
                    }

                    String completionMessage;
                    if (this.state == AgentState.FINISHED) {
                        // 如果是doTerminate等工具设置的FINISHED
                        completionMessage = "执行结束：智能体 " + agentName + " 已完成任务。";
                        log.info("Agent {} - Stream finished task explicitly.", agentName);
                    } else { // 因达到 maxSteps 而结束
                        this.state = AgentState.FINISHED; // 确保状态更新
                        completionMessage = "执行结束：智能体 " + agentName + " 已达到最大步骤数 (" + maxSteps + ")。";
                        log.info("Agent {} - Stream reached max steps.", agentName);
                    }
                    sendSseEvent(emitter, completionEventName, completionMessage);
                    emitter.complete();

                } catch (Exception e) { // 捕获 step() 循环中的异常
                    this.state = AgentState.ERROR;
                    log.error("Agent " + agentName + " - Stream execution failed during steps", e);
                    sendSseEvent(emitter, errorEventName, "执行错误 (Agent: " + agentName + "): " + e.getMessage());
                    emitter.complete(); // 出错后也应完成 emitter
                } finally {
                    this.cleanup(); // 确保清理
                }
            } catch (Exception e) { // 捕获 runAsync 顶层的意外异常
                log.error("Agent " + agentName + " - Stream run encountered an unexpected error before or after steps loop", e);
                // 尝试发送一个最终的错误信息，如果emitter还可用
                sendSseEvent(emitter, errorEventName, "智能体 " + agentName + " 发生意外的异步处理错误。");
                emitter.completeWithError(e); // 以错误状态完成 emitter
            }
        });

        emitter.onTimeout(() -> {
            log.warn("Agent {} - SSE connection timed out. User prompt was: '{}'", agentName, userPrompt);
            if (this.state != AgentState.FINISHED && this.state != AgentState.ERROR) {
                this.state = AgentState.ERROR; // 标记状态
            }
            this.cleanup();
            // Emitter会自动完成，但如果想尝试发送最后一条消息，这里可能太晚
        });

        emitter.onCompletion(() -> {
            log.info("Agent {} - SSE connection completed. Final state: {}. User prompt was: '{}'", agentName, this.state, userPrompt);
            // 确保如果仍在运行中被外部完成，状态得到更新
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED; // 或 ERROR，取决于完成原因
            }
            this.cleanup(); // 再次确保清理
        });
        return emitter;
    }

    // 辅助方法，用于发送带名称的SSE事件，并处理IOException
    private void sendSseEvent(SseEmitter emitter, String eventName, String data) {
        try {
            if (!emitter.toString().contains("CLOSED")) { // 简单的检查，避免向已关闭的emitter发送
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } else {
                log.warn("Emitter for agent {} was closed, could not send event '{}' with data: {}", getName(), eventName, data);
            }
        } catch (IOException e) {
            // IOException通常意味着客户端已断开连接
            log.warn("Failed to send SSE event '{}' for agent {}: {}. Client might have disconnected.", eventName, getName(), e.getMessage());
            // 在这种情况下，可以考虑调用 emitter.completeWithError(e) 或让它自然超时/完成
        } catch (IllegalStateException e) {
            // IllegalStateException (e.g., "SseEmitter has already been completed")
            log.warn("Failed to send SSE event '{}' for agent {}: {}. Emitter might have already completed.", eventName, getName(), e.getMessage());
        }
    }
}