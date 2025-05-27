package com.dali.fuaiagent.agent;


import com.dali.fuaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public abstract class BaseAgent {

    private String name;

    private String systemPrompt;
    private String nextStepPrompt;

    private AgentState state = AgentState.IDLE;

    private int maxSteps;
    private int currentStep;

    private ChatClient chatClient;

    // Memory ( 自主维护 )
    private List<Message> messageList = new ArrayList<>();

    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Agent is not in IDLE state, current state: " + this.state);
        }
        if (StringUtil.isBlank(userPrompt)) {
            throw new RuntimeException("User message cannot be blank");
        }

        state = AgentState.RUNNING;

        messageList.add(new UserMessage(userPrompt));

        List<String> results =  new ArrayList<>();

        try {
            for (int i=0; i < maxSteps && state !=  AgentState.FINISHED; i++) {
                int stepNumber =i + 1 ;
                currentStep = stepNumber;
                log.info("Executing step" + stepNumber + "/" + maxSteps);
                String stepResult = step();
                String result = "Step" + stepNumber + ": " + stepResult;
                results.add(result);
            }

            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated Reached max steps: " + maxSteps);
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error running agent", e);
            return "Error: " + e.getMessage();

        } finally {
            this.cleanup();
        }
    }

    public abstract String step();

    public void cleanup() {
    }


}
