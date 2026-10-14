package com.acme.llmapp.orchestration;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * AI Orchestration Layer — LangChain4j AiService.
 *
 * LangChain4j generates the implementation at startup via @AiService.
 * The framework auto-wires:
 *   • OpenAI ChatModel       (from application.yml)
 *   • ChatMemoryProvider     (per-session sliding-window memory)
 *   • RetrievalAugmentor     (RAG pipeline — see RagConfig)
 *
 * This is the equivalent of a LangChain4j "Chain":
 *   user message → retrieve context → build prompt → call LLM → return reply
 */
@AiService
public interface AssistantService {

    @SystemMessage("""
            You are a helpful enterprise AI assistant deployed inside a
            Java Spring Boot application built with LangChain4j.
            Answer questions concisely and professionally.
            Use any retrieved knowledge-base context that is provided to you.
            If you don't know the answer, say so clearly rather than guessing.
            """)
    String chat(@MemoryId String sessionId, @UserMessage String userMessage);
}
