package com.dali.fuaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
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

        String message = "adadad,明天去干嘛？";
        String response = fuFuApp.doChatWithRag(message, chatId);

    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("周末想带你去上海约会，推荐几个适合情侣的小众打卡地？");

        // 测试网页抓取：恋爱案例分析
        testMessage("你能帮我看看网站（pic.furinabox.me）有什么内容吗？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张图片从（https://furinabox.me/furina.jpg）");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存你刚才的操作步骤为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = fuFuApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

}