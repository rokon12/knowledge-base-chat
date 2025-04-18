package ca.bazlur;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class KnowledgeAssistant {

  private static final Logger logger = LoggerFactory.getLogger(KnowledgeAssistant.class);

  interface Assistant {
    String chat(String userMessage);
  }

  public static void main(String[] args) {
    try {
      EmbeddingStore<TextSegment> embeddingStore = KnowledgeBaseIngestor.ingestData();

      logger.info("Initializing OpenAI Chat Model...");
      AppConfig config = AppConfig.getInstance();
      ChatLanguageModel chatModel = OpenAiChatModel.builder()
              .apiKey(config.getApiKey())
              .modelName(config.getChatModelName())
              .logRequests(config.isLogRequests())
              .logResponses(config.isLogResponses())
              .build();

      EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
              .apiKey(config.getApiKey())
              .modelName(config.getEmbeddingModelName())
              .logRequests(config.isLogRequests())
              .logResponses(config.isLogResponses())
              .build();

      logger.info("Chat Model initialized.");


      logger.info("Initializing Content Retriever...");
      ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
              .embeddingStore(embeddingStore)
              .embeddingModel(embeddingModel)
              .maxResults(config.getMaxResults())
              .minScore(config.getMinScore())
              .build();
      logger.info("Content Retriever configured with maxResults={}, minScore={}", 
              config.getMaxResults(), config.getMinScore());
      logger.info("Content Retriever initialized.");

      ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(config.getChatMemoryMessages());
      logger.info("Chat Memory initialized (window size {}).", config.getChatMemoryMessages());

      logger.info("Creating AI Service...");
      Assistant assistant = AiServices.builder(Assistant.class)
              .chatLanguageModel(chatModel)
              .contentRetriever(contentRetriever)
              .chatMemory(chatMemory)
              .build();
      logger.info("AI Service created. Assistant is ready.");

      Scanner scanner = new Scanner(System.in);
      logger.info("Assistant is ready to chat");
      System.out.println("\nAssistant: Hello! Ask me about the system components or known issues.");

      while (true) {
        System.out.print("You: ");
        String userQuery = scanner.nextLine();

        if ("exit".equalsIgnoreCase(userQuery)) {
          logger.info("User requested to exit");
          System.out.println("Assistant: Goodbye!");
          break;
        }

        logger.debug("Processing user query: {}", userQuery);
        String assistantResponse = assistant.chat(userQuery);
        System.out.println("Assistant: \n" + assistantResponse);
      }
      scanner.close();

    } catch (Exception e) {
      logger.error("An error occurred during assistant setup or chat", e);
    }
  }
}
