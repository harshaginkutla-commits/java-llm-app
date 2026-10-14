package com.acme.llmapp.service;

import com.acme.llmapp.dto.ChatRequest;
import com.acme.llmapp.dto.ChatResponse;
import com.acme.llmapp.model.ChatMessage;
import com.acme.llmapp.orchestration.AssistantService;
import com.acme.llmapp.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service Layer — Business logic, orchestration coordination, prompt management.
 *
 * Responsibilities:
 *  1. Validate / enrich the incoming request
 *  2. Delegate to the AI Orchestration layer (LangChain4j AssistantService)
 *  3. Persist both turns via the Data Access layer (JPA)
 *  4. Build and return a structured ChatResponse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AssistantService assistantService;        // AI Orchestration Layer
    private final ChatMessageRepository messageRepository;  // Data Access Layer

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        String sessionId = request.getSessionId();
        String userText  = request.getMessage();

        log.info("Chat [session={}] user: {}", sessionId, userText);

        // ── Persist user message ──────────────────────────────────────────────
        messageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.Role.USER)
                .content(userText)
                .build());

        // ── AI Orchestration (LangChain4j chain: RAG → prompt → LLM) ─────────
        String reply = assistantService.chat(sessionId, userText);

        log.info("Chat [session={}] assistant: {}", sessionId, reply);

        // ── Persist assistant reply ───────────────────────────────────────────
        messageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.Role.ASSISTANT)
                .content(reply)
                .build());

        return ChatResponse.builder()
                .sessionId(sessionId)
                .reply(reply)
                .timestamp(Instant.now())
                .build();
    }

    /** Return full conversation history for a session. */
    @Transactional(readOnly = true)
    public List<ChatMessage> getHistory(String sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /** Clear conversation history and LangChain4j in-memory for a session. */
    @Transactional
    public void clearSession(String sessionId) {
        messageRepository.deleteBySessionId(sessionId);
        log.info("Cleared session {}", sessionId);
    }
}
