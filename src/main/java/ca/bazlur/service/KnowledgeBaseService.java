package ca.bazlur.service;

import ca.bazlur.config.AIProvider;
import ca.bazlur.config.ConfigProvider;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
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

/**
 * Service class that handles loading and processing knowledge base data.
 */
public class KnowledgeBaseService {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseService.class);

    private final ConfigProvider config;
    private final List<String> resourceNames;

    /**
     * Creates a new KnowledgeBaseService with the given configuration.
     *
     * @param config The application configuration
     * @param resourceNames The names of the resource files containing knowledge base data
     */
    public KnowledgeBaseService(ConfigProvider config, List<String> resourceNames) {
        this.config = config;
        this.resourceNames = resourceNames;
    }

    /**
     * Creates a new KnowledgeBaseService with the given configuration and default resource names.
     *
     * @param config The application configuration
     */
    public KnowledgeBaseService(ConfigProvider config) {
        this(config, List.of("components.txt", "knowledge.txt"));
    }

    /**
     * Loads and processes the knowledge base data.
     *
     * @return An embedding store containing the processed knowledge base data
     * @throws RuntimeException if there is an error loading or processing the data
     */
    public EmbeddingStore<TextSegment> loadKnowledgeBase() {
        logger.info("Starting knowledge base ingestion...");

        List<Document> documents = loadDocuments();
        EmbeddingModel embeddingModel = createEmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = createEmbeddingStore();
        DocumentSplitter splitter = createDocumentSplitter();

        ingestDocuments(documents, embeddingModel, embeddingStore, splitter);

        return embeddingStore;
    }

    /**
     * Loads documents from the configured resource files.
     *
     * @return A list of loaded documents
     * @throws RuntimeException if there is an error loading the documents
     */
    private List<Document> loadDocuments() {
        List<Document> documents = new ArrayList<>();
        DocumentParser parser = new TextDocumentParser();

        try {
            for (String resourceName : resourceNames) {
                logger.info("Loading document from resource: {}", resourceName);
                documents.add(loadDocumentFromResource(resourceName, parser));
            }

            logger.info("Documents loaded successfully.");
            return documents;

        } catch (IOException e) {
            logger.error("Failed to load documents from resources", e);
            throw new RuntimeException("Failed to load knowledge base documents", e);
        } catch (NullPointerException e) {
            logger.error("A required resource file was not found.", e);
            throw new RuntimeException("Failed to find knowledge base documents", e);
        }
    }

    /**
     * Creates an embedding model using the configured settings.
     *
     * @return The configured embedding model
     */
    private EmbeddingModel createEmbeddingModel() {
        AIProvider provider = config.getAIProvider();
        logger.info("Initializing {} Embedding Model...", provider);

        EmbeddingModel embeddingModel;
        if (provider == AIProvider.OPENAI) {
            embeddingModel = OpenAiEmbeddingModel.builder()
                    .apiKey(config.getApiKey())
                    .modelName(config.getEmbeddingModelName())
                    .logRequests(config.isLogRequests())
                    .logResponses(config.isLogResponses())
                    .build();
        } else {
            embeddingModel = OllamaEmbeddingModel.builder()
                    .baseUrl(config.getBaseUrl())
                    .modelName(config.getEmbeddingModelName())
                    .logRequests(config.isLogRequests())
                    .logResponses(config.isLogResponses())
                    .build();
        }

        logger.info("Embedding Model initialized.");
        return embeddingModel;
    }

    /**
     * Creates an in-memory embedding store.
     *
     * @return The created embedding store
     */
    private EmbeddingStore<TextSegment> createEmbeddingStore() {
        logger.info("Initializing In-Memory Embedding Store...");
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        logger.info("Embedding Store initialized.");
        return embeddingStore;
    }

    /**
     * Creates a document splitter using the configured settings.
     *
     * @return The configured document splitter
     */
    private DocumentSplitter createDocumentSplitter() {
        DocumentSplitter splitter = DocumentSplitters.recursive(
                config.getChunkSize(), 
                config.getChunkOverlap()
        );
        logger.info("Using recursive document splitter ({} chars, {} overlap).",
                config.getChunkSize(), config.getChunkOverlap());
        return splitter;
    }

    /**
     * Ingests documents into the embedding store.
     *
     * @param documents The documents to ingest
     * @param embeddingModel The embedding model to use
     * @param embeddingStore The embedding store to ingest into
     * @param splitter The document splitter to use
     */
    private void ingestDocuments(
            List<Document> documents,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            DocumentSplitter splitter
    ) {
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        logger.info("Ingesting documents into the embedding store...");
        ingestor.ingest(documents);

        // Log the number of documents ingested
        // Note: The actual number of segments may be different due to document splitting
        logger.info("Ingestion complete. {} documents ingested into the embedding store.", documents.size());
    }

    /**
     * Loads a document from a resource file.
     *
     * @param resourceName The name of the resource file
     * @param parser The document parser to use
     * @return The loaded document
     * @throws IOException if there is an error loading the document
     * @throws NullPointerException if the resource file is not found
     */
    private Document loadDocumentFromResource(String resourceName, DocumentParser parser) throws IOException {
        try (InputStream inputStream = getResourceAsStream(resourceName)) {
            Objects.requireNonNull(inputStream, "Resource not found: " + resourceName);
            return parser.parse(inputStream);
        }
    }

    /**
     * Gets an input stream for a resource.
     * This method is protected to allow overriding in tests.
     *
     * @param resourceName The name of the resource
     * @return An input stream for the resource, or null if the resource is not found
     */
    protected InputStream getResourceAsStream(String resourceName) {
        return getClass().getClassLoader().getResourceAsStream(resourceName);
    }
}
