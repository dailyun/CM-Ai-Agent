package com.dali.fuaiagent.tool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "furinabox.pdf";
        String content = " https://pic.furinabox.me";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}
