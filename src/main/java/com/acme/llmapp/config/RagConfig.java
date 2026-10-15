package com.acme.llmapp.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Vector DB / Knowledge Base + RAG pipeline configuration.
 *
 * Default:  InMemoryEmbeddingStore (works with zero infrastructure).
 * Production: swap the embeddingStore() bean for PgVectorEmbeddingStore
 *             (see commented block below) with the 'postgres' Spring profile.
 *
 * Documents in src/main/resources/documents/ are ingested automatically
 * on startup so the RAG pipeline has something to search from day one.
 */
@Configuration
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    // Local embedding model — runs in-process, no API key required
    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    private final InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

    @Value("${app.rag.max-results:3}")
    private int maxResults;

    @Value("${app.rag.min-score:0.6}")
    private double minScore;

    // ── Embedding Store ───────────────────────────────────────────────────────

    /**
     * In-memory store — perfect for local dev and demos.
     *
     * To switch to pgvector in production, replace this bean with:
     *
     * <pre>{@code
     * @Bean
     * @Profile("postgres")
     * public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource) {
     *     return PgVectorEmbeddingStore.builder()
     *             .datasource(dataSource)
     *             .table("embeddings")
     *             .dimension(1536)          // text-embedding-3-small
     *             .createTable(true)
     *             .build();
     * }
     * }</pre>
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return store;
    }

    // ── Chat Memory (per-session sliding window) ──────────────────────────────

    @Bean
    public Supplier<MessageWindowChatMemory> chatMemoryProvider() {
        return () -> MessageWindowChatMemory.withMaxMessages(20);
    }

    // ── RAG Content Retriever ─────────────────────────────────────────────────

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> store) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }

    // ── Document Ingestion on Startup ─────────────────────────────────────────

    @PostConstruct
    public void ingestDocuments() {
        List<Document> docs = new ArrayList<>();

        // Load any .txt / .pdf files bundled under resources/documents/
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:documents/*");
            for (Resource r : resources) {
                try {
                    Path path = r.getFile().toPath();
                    docs.add(FileSystemDocumentLoader.loadDocument(path));
                    log.info("RAG: ingested {}", r.getFilename());
                } catch (Exception e) {
                    log.warn("RAG: could not load {}: {}", r.getFilename(), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.warn("RAG: no documents found in resources/documents/ — skipping ingestion");
        }

        if (docs.isEmpty()) {
            log.info("RAG: no documents to ingest — add .txt/.pdf files to src/main/resources/documents/");
            return;
        }

        try {
            DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(splitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(store)
                    .build();

            ingestor.ingest(docs);
            log.info("RAG: ingested {} document(s) into the embedding store", docs.size());
        } catch (Exception e) {
            log.warn("RAG: document ingestion failed (app will start without pre-loaded knowledge): {}", e.getMessage());
            log.warn("RAG: to enable RAG, ensure your OpenAI API key has credits at https://platform.openai.com/settings/organization/billing/");
        }
    }
}
