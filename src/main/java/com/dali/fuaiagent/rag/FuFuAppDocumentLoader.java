package com.dali.fuaiagent.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class FuFuAppDocumentLoader {

    private final ResourcePatternResolver resourcePatterResolver;

    FuFuAppDocumentLoader(ResourcePatternResolver resourcePatterResolver) {
        this.resourcePatterResolver = resourcePatterResolver;
    }

    public List<Document> loadMarkdowns() {
        List<Document> allDocuments =new ArrayList<>();
        try {
            Resource[] resources = resourcePatterResolver.getResources("classpath*:document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("fileName", fileName)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.read());
            }
        } catch (IOException e) {
            log.error("MarkDown Read Error", e);
        }
        return allDocuments;
    }
}
