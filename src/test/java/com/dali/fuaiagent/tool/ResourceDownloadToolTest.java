package com.dali.fuaiagent.tool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ResourceDownloadToolTest {

    @Test
    public void testDownloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://furinabox.me/furina.jpg";
        String fileName = "furina.jpg";
        String result = tool.downloadResource(url, fileName);
        assertNotNull(result);
    }
}
