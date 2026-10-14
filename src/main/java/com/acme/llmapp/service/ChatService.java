package com.acme.llmapp.service;

import com.acme.llmapp.dto.ChatRequest;
import com.acme.llmapp.dto.ChatResponse;
import com.acme.llmapp.model.ChatMessage;
import com.acme.llmapp.orchestration.AssistantService;
import com.acme.llmapp.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final AssistantService assistantService;
    private final ChatMessageRepository messageRepository;

    public ChatService(AssistantService assistantService, ChatMessageRepository messageRepository) {
        this.assistantService = assistantService;
        this.messageRepository = messageRepository;
    }

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
