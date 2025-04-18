package ca.bazlur.service;

import ca.bazlur.config.AIProvider;
import ca.bazlur.config.ConfigProvider;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class that handles the assistant functionality. This class is responsible for creating
 * and configuring the AI assistant.
 */
public class AssistantService {
  private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);

  private final ConfigProvider config;
  private final EmbeddingStore<TextSegment> embeddingStore;

  /** Interface defining the assistant's capabilities. */
  public interface Assistant {

    @SystemMessage("""
					You are an AI assistant specialized in querying operational knowledge about technical systems 
					(components, status, faults, procedures). Answer user questions accurately and concisely, 
					relying *strictly* on the information provided in the context. Do not use any prior knowledge or make assumptions.
					Return result in markdwon format.""")
    String chat(String userMessage);
  }

  private Assistant assistant;

  /**
   * Creates a new AssistantService with the given configuration and embedding store.
   *
   * @param config The application configuration
   * @param embeddingStore The embedding store containing the knowledge base
   */
  public AssistantService(ConfigProvider config, EmbeddingStore<TextSegment> embeddingStore) {
    this.config = config;
    this.embeddingStore = embeddingStore;
    initialize();
  }

  /**
   * Initializes the assistant with the configured models and settings. This method is protected to
   * allow overriding in tests.
   */
  protected void initialize() {
    AIProvider provider = config.getAIProvider();
    logger.info("Initializing {} Chat Model...", provider);
    ChatLanguageModel chatModel = createChatModel();
    logger.info("Chat Model initialized.");

    logger.info("Initializing {} Embedding Model...", provider);
    EmbeddingModel embeddingModel = createEmbeddingModel();
    logger.info("Embedding Model initialized.");

    logger.info("Initializing Content Retriever...");
    ContentRetriever contentRetriever = createContentRetriever(embeddingModel);
    logger.info("Content Retriever initialized.");

    ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(config.getChatMemoryMessages());
    logger.info("Chat Memory initialized (window size {}).", config.getChatMemoryMessages());

    logger.info("Creating AI Service...");
    assistant =
        AiServices.builder(Assistant.class)
            .chatLanguageModel(chatModel)
            .contentRetriever(contentRetriever)
            .chatMemory(chatMemory)
            .build();
    logger.info("AI Service created. Assistant is ready.");
  }

  /**
   * Creates a chat model using the configured settings.
   *
   * @return The configured chat model
   */
  private ChatLanguageModel createChatModel() {
    if (config.getAIProvider() == AIProvider.OPENAI) {
      return OpenAiChatModel.builder()
          .apiKey(config.getApiKey())
          .modelName(config.getChatModelName())
          .logRequests(config.isLogRequests())
          .logResponses(config.isLogResponses())
          .build();
    } else {
      return OllamaChatModel.builder()
          .baseUrl(config.getBaseUrl())
          .modelName(config.getChatModelName())
          .logRequests(config.isLogRequests())
          .logResponses(config.isLogResponses())
          .build();
    }
  }

  /**
   * Creates an embedding model using the configured settings.
   *
   * @return The configured embedding model
   */
  private EmbeddingModel createEmbeddingModel() {
    if (config.getAIProvider() == AIProvider.OPENAI) {
      return OpenAiEmbeddingModel.builder()
          .apiKey(config.getApiKey())
          .modelName(config.getEmbeddingModelName())
          .logRequests(config.isLogRequests())
          .logResponses(config.isLogResponses())
          .build();
    } else {
      return OllamaEmbeddingModel.builder()
          .baseUrl(config.getBaseUrl())
          .modelName(config.getEmbeddingModelName())
          .logRequests(config.isLogRequests())
          .logResponses(config.isLogResponses())
          .build();
    }
  }

  /**
   * Creates a content retriever using the configured settings.
   *
   * @param embeddingModel The embedding model to use
   * @return The configured content retriever
   */
  private ContentRetriever createContentRetriever(EmbeddingModel embeddingModel) {
    ContentRetriever contentRetriever =
        EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(config.getMaxResults())
            .minScore(config.getMinScore())
            .build();
    logger.info(
        "Content Retriever configured with maxResults={}, minScore={}",
        config.getMaxResults(),
        config.getMinScore());
    return contentRetriever;
  }

  /**
   * Processes a user message and returns the assistant's response.
   *
   * @param userMessage The user's message
   * @return The assistant's response
   */
  public String processMessage(String userMessage) {
    logger.debug("Processing user message: {}", userMessage);
    return assistant.chat(userMessage);
  }
}
