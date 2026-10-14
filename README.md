# ☕ Java + LLM Architecture

> **Spring Boot · LangChain4j · RAG · OpenAI / Azure OpenAI**  
> A production-ready enterprise AI application following the architecture from the diagram.

---

## Architecture

```
Users / Channels
      │
      ▼  ① User Request
┌─────────────────────────────────────────────┐
│          Spring Boot (Java Backend)          │
│                                              │
│  API Layer          ChatController.java      │
│  Service Layer      ChatService.java         │──► ③ Prompt + Context
│  AI Orchestration   AssistantService.java    │──► LLM Provider (OpenAI) - https://platform.openai.com/api-keys
│  Data Access        ChatMessageRepository    │◄── ④ LLM Response
│                     (JPA / Hibernate)        │
└─────────────────────────────────────────────┘
      │  ② Retrieve Context (RAG)
      ▼
Vector DB / Knowledge Base
  • InMemoryEmbeddingStore (dev)
  • pgvector / Pinecone      (prod)

Deployment & Infrastructure
  Docker · Kubernetes · Azure AKS
```

---

## Quick Start (local, no Docker)

```bash
# 1. Clone
git clone https://github.com/YOUR_USERNAME/java-llm-app.git
cd java-llm-app

# 2. Set your OpenAI key
export OPENAI_API_KEY=sk-...

# 3. Run (H2 in-memory DB, no Postgres needed)
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**

---

## Quick Start (Docker Compose — with pgvector)

```bash
cp .env.example .env          # add your OPENAI_API_KEY
docker compose up --build
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/chat` | Send a message, get an AI reply |
| `GET` | `/api/chat/{sessionId}/history` | Get conversation history |
| `DELETE` | `/api/chat/{sessionId}` | Clear a session |
| `POST` | `/api/documents/ingest` | Upload a .txt file to the RAG knowledge base |

### Example — Chat

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "user-123",
    "message": "What is the Enterprise plan price?"
  }'
```

```json
{
  "sessionId": "user-123",
  "reply": "The Enterprise plan is $499/month and includes unlimited API calls, SSO, and 24/7 support.",
  "retrievedSources": ["sample-knowledge-base.txt"],
  "timestamp": "2024-06-23T11:33:00Z"
}
```

### Example — Ingest a document into RAG

```bash
curl -X POST http://localhost:8080/api/documents/ingest \
  -F "file=@my-product-docs.txt"
```

---

## Project Structure

```
src/main/java/com/acme/llmapp/
├── LlmAppApplication.java          # Entry point
├── controller/
│   ├── ChatController.java         # API Layer — REST endpoints
│   └── DocumentController.java     # Document ingestion endpoint
├── service/
│   └── ChatService.java            # Service Layer — business logic
├── orchestration/
│   └── AssistantService.java       # AI Orchestration — LangChain4j AiService
├── config/
│   └── RagConfig.java              # Vector DB, RAG retriever, chat memory
├── model/
│   └── ChatMessage.java            # JPA entity
├── repository/
│   └── ChatMessageRepository.java  # Data Access Layer
└── dto/
    ├── ChatRequest.java
    └── ChatResponse.java

src/main/resources/
├── application.yml                 # Config (H2 default, postgres profile)
└── documents/
    └── sample-knowledge-base.txt   # Seeded RAG knowledge base
```

---

## Switching to Azure OpenAI

In `application.yml`, replace the `langchain4j.open-ai` block with:

```yaml
langchain4j:
  azure-open-ai:
    chat-model:
      endpoint: ${AZURE_OPENAI_ENDPOINT}
      api-key:  ${AZURE_OPENAI_KEY}
      deployment-name: gpt-4o
```

Add the Azure dependency to `pom.xml`:

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-azure-open-ai-spring-boot-starter</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

---

## Switching to pgvector (production)

1. Run with `--spring.profiles.active=postgres`
2. In `RagConfig.java`, uncomment the `PgVectorEmbeddingStore` bean
3. Start Postgres with `docker compose up postgres`

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.2 |
| AI Orchestration | LangChain4j 0.31 |
| LLM Provider | OpenAI GPT-4o / Azure OpenAI |
| RAG / Embeddings | text-embedding-3-small + pgvector |
| Data Access | Spring Data JPA, Hibernate |
| Database | H2 (dev) / PostgreSQL + pgvector (prod) |
| Deployment | Docker, Kubernetes, Azure AKS |
| Monitoring | Spring Actuator, Prometheus/Grafana ready |
