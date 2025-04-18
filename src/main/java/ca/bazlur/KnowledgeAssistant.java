package ca.bazlur;

import ca.bazlur.config.AppConfig;
import ca.bazlur.config.ConfigProvider;
import ca.bazlur.service.AssistantService;
import ca.bazlur.service.KnowledgeBaseService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Main class for the Knowledge Assistant application. Handles the user interface and coordinates
 * the services.
 */
public class KnowledgeAssistant {

  private static final Logger logger = LoggerFactory.getLogger(KnowledgeAssistant.class);

  public static void main(String[] args) {
    try {
      ConfigProvider config = AppConfig.create();
      logger.info("Configuration initialized");

      logger.info("Loading knowledge base...");
      KnowledgeBaseService knowledgeBaseService = new KnowledgeBaseService(config);
      EmbeddingStore<TextSegment> embeddingStore = knowledgeBaseService.loadKnowledgeBase();
      logger.info("Knowledge base loaded");

      logger.info("Initializing assistant service...");
      AssistantService assistantService = new AssistantService(config, embeddingStore);
      logger.info("Assistant service initialized");

      runChatInterface(assistantService);

    } catch (Exception e) {
      logger.error("An error occurred during assistant setup or chat", e);
      System.err.println("Error: " + e.getMessage());
      System.err.println("Please check the logs for more details.");
    }
  }

  /**
   * Runs the interactive chat interface for interacting with the assistant.
   *
   * @param assistantService The assistant service to use
   */
  private static void runChatInterface(AssistantService assistantService) {
    Scanner scanner = new Scanner(System.in);
    logger.info("Starting chat interface");

    System.out.println("\n======================================");
    System.out.println("     Welcome to Knowledge Assistant");
    System.out.println("======================================");
    System.out.println("Ask me anything based on my knowledge.");
    System.out.println("Type 'help' for commands or 'exit' to quit.\n");

    while (true) {
      System.out.print("\nYou: ");
      String userQuery = scanner.nextLine().trim();

      if (userQuery.isEmpty()) {
        System.out.println("\nAssistant: Please type a question or command ('help'/'exit').");
        continue;
      }

      if ("help".equalsIgnoreCase(userQuery)) {
        printHelp();
        continue;
      }

      if ("exit".equalsIgnoreCase(userQuery)) {
        logger.info("User requested to exit");
        System.out.println(
            "\nAssistant: Goodbye! It was nice chatting with you."); // Friendlier exit
        break;
      }

      try {
        logger.debug("Processing user query: {}", userQuery);
        System.out.println("\nAssistant: Thinking...");
        String assistantResponse = assistantService.processMessage(userQuery);
        System.out.println("\nAssistant:\n");
        System.out.println(assistantResponse);
      } catch (Exception e) {
        logger.error("Error processing query '{}'", userQuery, e); // Log the query that failed
        System.out.println("\nAssistant: I'm sorry, I encountered an error trying to respond.");
        System.out.println(
            "             Please try rephrasing your question or ask something else.");
      }
    }

    scanner.close();
    logger.info("Chat interface closed");
  }

  private static void printHelp() {
    System.out.println("\nAssistant: How I can help:");
    System.out.println("  - Ask any question based on the loaded knowledge.");
    System.out.println("  - Type 'help' to see this message again.");
    System.out.println("  - Type 'exit' to quit the application.");
  }
}
