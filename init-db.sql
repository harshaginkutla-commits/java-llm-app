-- Enable pgvector extension for RAG embeddings
CREATE EXTENSION IF NOT EXISTS vector;

-- The embeddings table is created automatically by LangChain4j PgVectorEmbeddingStore
-- This file just ensures the extension is available before the app starts.
