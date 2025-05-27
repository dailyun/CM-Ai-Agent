package com.dali.fuaiagent.agent;

import com.dali.fuaiagent.advisor.MyLoggerAdvisor; // 假设这个类存在且无需修改
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class FuManus extends ToolCallAgent {

    public FuManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools); // 调用父类构造函数
        this.setName("FuManus");

        // 更新后的、更全面的系统提示
        String COMPREHENSIVE_SYSTEM_PROMPT = """
                你是一个全能的AI助手FuManus，旨在解决用户提出的任何任务。
                你有多种工具可供使用，可以高效地完成复杂请求。

                你的操作指南如下：
                1. 根据用户需求和对话历史，主动选择最合适的工具或工具组合。
                2. 对于复杂任务，你可以分解问题并逐步使用不同的工具来解决。
                3. 使用每个工具后，清晰地解释执行结果，并建议下一步操作或在需要时请求澄清。
                4. 如果你认为任务已完成，或者在没有用户更具体输入的情况下无法继续，或者用户表示希望停止，你必须使用 'doTerminate' 工具/函数调用来结束交互。如果你已经询问过用户具体需求但未收到明确任务，请不要重复询问“我能为您做什么？”。
                """;
        this.setSystemPrompt(COMPREHENSIVE_SYSTEM_PROMPT);

        // nextStepPrompt 仍然可以设置，但 ToolCallAgent.think() 的主要逻辑不再依赖于将其作为 UserMessage 注入
        // 它可能用于其他目的，或者如果 think() 中没有卡顿处理逻辑时作为备用。
        // 在当前的卡顿处理逻辑下，这个特定的 nextStepPrompt 内容可能不会被直接使用。
        String LEGACY_NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """;
        this.setNextStepPrompt(LEGACY_NEXT_STEP_PROMPT);

        this.setMaxSteps(20); // 最大步骤限制

        // 初始化 ChatClient
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor()) // 假设 MyLoggerAdvisor 是你的自定义 Advisor
                .build();
        this.setChatClient(chatClient);
    }
}