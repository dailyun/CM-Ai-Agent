package com.dali.fuaiagent.agent;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态决定下一步
     *
     * @return true if the agent need Act, false otherwise
     */
    public abstract boolean think();

    public abstract String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if ( !shouldAct) {
                return "No need to act";
            }
            return act();
        } catch (Exception e) {
            log.error("Error in think", e);
            return "Error in think: " + e.getMessage();
        }
    }

}
