package com.dali.fuaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class CMAppTest {

    @Resource
    private CMApp cmApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();

        String message = "你好,我是Dali，什么是Java中的Bean";
        String response = cmApp.doChat(message, chatId);

        message = "介绍Golang";
        response = cmApp.doChat(message, chatId);
        Assertions.assertNotNull(response);

        message = "简短总结之前的对话";
        response = cmApp.doChat(message, chatId);
        Assertions.assertNotNull(response);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好,我是Dali，什么是Java中的Bean";
        CMApp.CMReport report = cmApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(report);
    }
}