package com.springai.docmind.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfig {

    @Bean
    public OpenAPI openAPI(){

        return new OpenAPI()
                .info(
                        new Info()
                                .title("DocMind - AI Document Intelligence and RAG Backend")
                                .description("DocMind is an AI-powered document question-answering application built with Spring Boot and Spring AI. It uses OpenAI models to generate embeddings for uploaded documents and enables users to ask natural-language questions about their documents using a Retrieval-Augmented Generation (RAG) pipeline.")
                                .version("1.0.0")
                                .contact(new Contact()
                                        .name("Swastik Banerjee")
                                )
                );
    }

    @Bean
    public EmbeddingModel embeddingModel(){

        return new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                return null;
            }

            @Override
            public float[] embed(Document document) {
                return new float[0];
            }
        };
    }
}
