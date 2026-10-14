package com.acme.llmapp.dto;

import java.time.Instant;
import java.util.List;

public class ChatResponse {

    private String sessionId;
    private String reply;
    private List<String> retrievedSources;
    private Instant timestamp;

    public ChatResponse() {}

    public ChatResponse(String sessionId, String reply, List<String> retrievedSources, Instant timestamp) {
        this.sessionId = sessionId;
        this.reply = reply;
        this.retrievedSources = retrievedSources;
        this.timestamp = timestamp;
    }

    public static Builder builder() { return new Builder(); }

    public String getSessionId() { return sessionId; }
    public String getReply() { return reply; }
    public List<String> getRetrievedSources() { return retrievedSources; }
    public Instant getTimestamp() { return timestamp; }

    public static class Builder {
        private String sessionId;
        private String reply;
        private List<String> retrievedSources;
        private Instant timestamp;

        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public Builder reply(String reply) { this.reply = reply; return this; }
        public Builder retrievedSources(List<String> retrievedSources) { this.retrievedSources = retrievedSources; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public ChatResponse build() {
            return new ChatResponse(sessionId, reply, retrievedSources, timestamp);
        }
    }
}
