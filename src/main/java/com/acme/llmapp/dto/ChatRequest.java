package com.acme.llmapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "sessionId must not be blank")
    private String sessionId;

    @NotBlank(message = "message must not be blank")
    private String message;
}
