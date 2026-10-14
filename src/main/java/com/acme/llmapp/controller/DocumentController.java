package com.acme.llmapp.controller;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * API Layer — Document ingestion endpoint.
 *
 * POST /api/documents/ingest   → upload a .txt file into the RAG knowledge base
 *
 * For PDF ingestion wire in ApachePdfBoxDocumentParser instead of
 * reading the bytes as a string.
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel              embeddingModel;

    public DocumentController(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, String>> ingest(
            @RequestParam("file") MultipartFile file) throws Exception {

        String filename = file.getOriginalFilename();
        String text     = new String(file.getBytes(), StandardCharsets.UTF_8);

        Document doc = Document.from(text, Metadata.from("source", filename));

        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(doc);
        log.info("Ingested document: {}", filename);

        return ResponseEntity.ok(Map.of(
                "status",   "ingested",
                "filename", filename != null ? filename : "unknown"
        ));
    }
}
