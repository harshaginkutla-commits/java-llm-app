package com.acme.llmapp.controller;

import com.acme.llmapp.dto.ChatRequest;
import com.acme.llmapp.dto.ChatResponse;
import com.acme.llmapp.model.ChatMessage;
import com.acme.llmapp.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API Layer — REST Controllers / Endpoints.
 *
 * POST /api/chat              → send a message, get an AI reply
 * GET  /api/chat/{sessionId}  → fetch conversation history
 * DELETE /api/chat/{sessionId}→ clear a session
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")   // tighten in production
public class ChatController {

    private final ChatService chatService;

    /**
     * Main endpoint — user sends a message, receives an AI-generated reply.
     * The full pipeline fires here: RAG retrieval → prompt assembly → LLM → persist.
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the full conversation history for a given session,
     * ordered oldest-first.
     */
    @GetMapping("/{sessionId}/history")
    public ResponseEntity<List<ChatMessage>> history(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatService.getHistory(sessionId));
    }

    /**
     * Clears the conversation history and LangChain4j chat memory for a session.
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
