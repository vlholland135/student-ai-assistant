package edu.bellevue.cis530.studentai.config;

import edu.bellevue.cis530.studentai.service.DocumentLoaderService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class holds the beans that the rest of the project needs.
 * We keep these beans here so the controller and service classes stay clean.
 */
@Configuration
public class StudentAiConfig {

    /**
     * We need a ChatClient bean because all four endpoints send prompts to the model.
     * This bean also gives every request a helpful default system instruction.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are a friendly student support assistant for a Spring Boot course.
                        Explain ideas clearly and accurately for a student who is learning Spring Boot and Spring AI.
                        Keep answers focused and practical, and use short examples when they help.
                        """)
                .build();
    }

    /**
     * We need a VectorStore bean for the RAG endpoint.
     * SimpleVectorStore is a small in-memory store that works well for classroom demos.
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * We need this runner so that when the project starts, the local text files
     * are loaded into the vector store automatically.
     *
     * The API-key check keeps startup friendlier for students. If the key is missing,
     * the app can still start and students can see the project structure first.
     */
    @Bean
    public ApplicationRunner loadVectorStore(
            VectorStore vectorStore,
            DocumentLoaderService documentLoaderService,
            @Value("${spring.ai.openai.api-key:}") String apiKey) {

        return args -> {
            if (apiKey == null || apiKey.isBlank() || apiKey.contains("${")) {
                System.out.println("Skipping vector-store loading because the OpenAI API key is missing or unresolved.");
                return;
            }

            vectorStore.add(documentLoaderService.loadRagDocuments());
            System.out.println("RAG documents loaded into the vector store.");
        };
    }
}
