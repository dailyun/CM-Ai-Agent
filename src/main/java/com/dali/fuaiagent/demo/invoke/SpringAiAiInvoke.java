package com.dali.fuaiagent.demo.invoke;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SpringAiAiInvoke implements CommandLineRunner {
    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage  message = dashscopeChatModel.call(new Prompt("What is the meaning of life?Speak with chinese"))
                .getResult()
                .getOutput();
        System.out.println(message.getText());
    }
}
