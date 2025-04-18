package ca.bazlur;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
// Remove FileSystemDocumentLoader import if no longer needed elsewhere
// import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KnowledgeBaseIngestor {

  private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseIngestor.class);

  public static EmbeddingStore<TextSegment> ingestData() {
    logger.info("Starting knowledge base ingestion...");

    List<Document> documents = new ArrayList<>();
    // Use the appropriate parser for your document types
    DocumentParser parser = new TextDocumentParser();

    try {
      logger.info("Loading document from resource: components.txt");
      documents.add(loadDocumentFromResource("components.txt", parser));

      logger.info("Loading document from resource: knowledge.txt");
      documents.add(loadDocumentFromResource("knowledge.txt", parser));

      logger.info("Documents loaded successfully.");

    } catch (IOException e) {
      logger.error("Failed to load documents from resources", e);
      // Consider how to handle this error - rethrow, return empty store, etc.
      throw new RuntimeException("Failed to load knowledge base documents", e);
    } catch (NullPointerException e) {
      // This catches the case where getResourceAsStream returns null
      logger.error("A required resource file was not found.", e);
      throw new RuntimeException("Failed to find knowledge base documents", e);
    }

    logger.info("Initializing OpenAI Embedding Model...");
    AppConfig config = AppConfig.getInstance();
    EmbeddingModel embeddingModel =
            OpenAiEmbeddingModel.builder()
                    .apiKey(config.getApiKey())
                    .modelName(config.getEmbeddingModelName())
                    .logRequests(config.isLogRequests())
                    .logResponses(config.isLogResponses())
                    .build();

    logger.info("Embedding Model initialized.");

    logger.info("Initializing In-Memory Embedding Store...");
    EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    logger.info("Embedding Store initialized.");

    DocumentSplitter splitter = DocumentSplitters.recursive(config.getChunkSize(), config.getChunkOverlap());
    logger.info("Using recursive document splitter ({} chars, {} overlap).",
            config.getChunkSize(), config.getChunkOverlap());

    EmbeddingStoreIngestor ingestor =
            EmbeddingStoreIngestor.builder()
                    .documentSplitter(splitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

    logger.info("Ingesting documents into the embedding store...");
    ingestor.ingest(documents);
    // Use embeddingStore.size() for a more accurate count after potential splitting/failures
    logger.info("Ingestion complete. Embedding store contains {} entries.", documents.size());

    return embeddingStore;
  }

  private static Document loadDocumentFromResource(String resourceName, DocumentParser parser) throws IOException {
    try (InputStream inputStream = KnowledgeBaseIngestor.class.getClassLoader().getResourceAsStream(resourceName)) {
      Objects.requireNonNull(inputStream, "Resource not found: " + resourceName);
      return parser.parse(inputStream);
    }
  }

  public static void main(String[] args) {
    try {
      logger.info("Starting standalone ingestion test");
      EmbeddingStore<TextSegment> store = ingestData();
      logger.info("Ingestion test completed successfully.");
    } catch (Exception e) {
      logger.error("An error occurred during ingestion", e);
    }
  }
}