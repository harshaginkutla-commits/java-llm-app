package com.acme.llmapp;

import com.acme.llmapp.dto.ChatRequest;
import com.acme.llmapp.dto.ChatResponse;
import com.acme.llmapp.orchestration.AssistantService;
import com.acme.llmapp.repository.ChatMessageRepository;
import com.acme.llmapp.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @MockBean
    private AssistantService assistantService;

    @Autowired
    private ChatMessageRepository repository;

    @Test
    void chat_persistsBothTurnsAndReturnsReply() {
        when(assistantService.chat(anyString(), anyString()))
                .thenReturn("Hello from the mocked LLM!");

        ChatRequest req = new ChatRequest();
        req.setSessionId("test-session-001");
        req.setMessage("Hello");

        ChatResponse resp = chatService.chat(req);

        assertThat(resp.getReply()).isEqualTo("Hello from the mocked LLM!");
        assertThat(resp.getSessionId()).isEqualTo("test-session-001");

        // Both user + assistant turns should be persisted
        var history = chatService.getHistory("test-session-001");
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getContent()).isEqualTo("Hello");
        assertThat(history.get(1).getContent()).isEqualTo("Hello from the mocked LLM!");
    }

    @Test
    void clearSession_removesHistory() {
        when(assistantService.chat(anyString(), anyString())).thenReturn("Hi");

        ChatRequest req = new ChatRequest();
        req.setSessionId("test-session-002");
        req.setMessage("Hi there");
        chatService.chat(req);

        chatService.clearSession("test-session-002");

        assertThat(chatService.getHistory("test-session-002")).isEmpty();
    }
}
