package com.dali.fuaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

// 如果 ThinkResult 是独立文件，需要 import
// import com.dali.fuaiagent.agent.ThinkResult;


// ThinkResult 也可以定义为 ReActAgent 的静态内部类
// class ThinkResult { ... }

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * 智能体思考并决定下一步是否需要行动（调用工具）。
     * @return ThinkResult 对象，包含是否行动的标志和AI的文本思考/回复。
     */
    public abstract ThinkResult think(); // 修改返回类型

    /**
     * 如果 think() 决定需要行动，则执行工具调用。
     * @return 工具执行结果的摘要字符串。
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            ThinkResult thinkResult = think(); // 调用已修改签名的 think()

            if (!thinkResult.shouldAct) {
                // think() 方法决定不需要调用工具。
                // 返回AI在该步骤的文本思考或回复。
                if (thinkResult.getAssistantThoughtOrResponse() != null && !thinkResult.getAssistantThoughtOrResponse().isBlank()) {
                    return thinkResult.getAssistantThoughtOrResponse();
                }
                return "AI思考完毕，无进一步工具操作或明确回复。"; // 作为后备回复
            }
            // think() 决定需要调用工具，所以执行 act()
            return act();
        } catch (Exception e) {
            log.error("智能体 " + getName() + " 在步骤 " + getCurrentStep() + " 的 step() 方法中出错 (think 或 act 阶段)", e);
            return "步骤执行中发生错误: " + e.getMessage();
        }
    }
}