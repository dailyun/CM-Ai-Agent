package com.dali.fuaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.document.Document;

import java.util.List;

@Configuration
public class FuFuAppVectorStoreConfig {

    @Resource
    private FuFuAppDocumentLoader fufuAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore fufuAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
        // 加载文档
        List<Document> documents = fufuAppDocumentLoader.loadMarkdowns();
//        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);
         List<Document> enrichDocuments =myKeywordEnricher.enrichDocuments(documents);
        simpleVectorStore.add(enrichDocuments);
        return simpleVectorStore;
    }
}
