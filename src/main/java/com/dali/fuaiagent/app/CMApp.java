package com.dali.fuaiagent.app;



import com.dali.fuaiagent.advisor.MyLoggerAdvisor;
import com.dali.fuaiagent.advisor.ReReadingAdvisor;
import com.dali.fuaiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class CMApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            你是一位编程导师，你需要根据用户的问题，给出一个完整的代码示例，并解释代码的逻辑。
            
            用户问题：
            """;

    public CMApp(ChatModel dashscopeChatModel) {

        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
//
//        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(

                        new MessageChatMemoryAdvisor(chatMemory)

                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;

    }



    record CMReport(String title, List<String> contents) {

    }
    public CMReport doChatWithReport(String message, String chatId) {
        CMReport report = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后生成结果，标题为用户的核心问题，内容为知识点的列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(CMReport.class);
        log.info("report: {}", report);
        return report;
    }
}
