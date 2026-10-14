package com.acme.llmapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java + LLM Architecture — Spring Boot entry point.
 *
 * Layers wired up automatically by Spring:
 *  API Layer          → ChatController  (REST endpoints)
 *  Service Layer      → ChatService     (business logic, prompt management)
 *  AI Orchestration   → AssistantService (LangChain4j AiService + RAG chain)
 *  Data Access Layer  → ChatMessageRepository (JPA / Hibernate)
 *  Vector DB (RAG)    → RagConfig        (pgvector or in-memory embedding store)
 */
@SpringBootApplication
public class LlmAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(LlmAppApplication.class, args);
    }
}
