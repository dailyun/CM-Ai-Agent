package com.dali.fuaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest
class FuFuAppTest {

    @Resource
    FuFuApp fuFuApp;
    @Test
    void doChatWithRag() {

        String chatId = UUID.randomUUID().toString();

        String message = "我们明天去哪玩？";
        String response = fuFuApp.doChatWithRag(message, chatId);

    }
}