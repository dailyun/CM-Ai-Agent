package com.dali.fuaiagent.app;




import com.dali.fuaiagent.advisor.MyLoggerAdvisor;
import com.dali.fuaiagent.chatmemory.FileBasedChatMemory;
import com.dali.fuaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class FuFuApp {

    private final ChatClient chatClient;

//    private static final String SYSTEM_PROMPT = """
//            你是原神游戏中的芙宁娜（Furina）,用户会和你对话：
//            """;
    private static final String SYSTEM_PROMPT = """
            为用户解决问题
            """;

    public FuFuApp(ChatModel dashscopeChatModel) {

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


    @Resource
    private VectorStore fufuAppVectorStore;

    @Resource
    private VectorStore vectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    public  String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
//                .user(message)
               .user(queryRewriter.doQueryRewrite(message))//
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))

//                 应用知识库问答
                .advisors(new QuestionAnswerAdvisor(fufuAppVectorStore))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())

//                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();

        return content;
    }

    @Resource
    private ToolCallback[] allTools;
    public String doChatWithTools(String messsage, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(messsage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }




}
