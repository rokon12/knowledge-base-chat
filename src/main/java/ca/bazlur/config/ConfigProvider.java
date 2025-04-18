package ca.bazlur.config;

/**
 * Interface for providing configuration values.
 * This allows for easier testing by enabling dependency injection of configuration.
 */
public interface ConfigProvider {
    /**
     * Gets the selected AI provider.
     *
     * @return The AI provider
     */
    AIProvider getAIProvider();

    /**
     * Gets the chat model name for the selected provider.
     *
     * @return The chat model name
     */
    String getChatModelName();

    /**
     * Gets the embedding model name for the selected provider.
     *
     * @return The embedding model name
     */
    String getEmbeddingModelName();

    /**
     * Gets the API key for the selected provider.
     *
     * @return The API key
     */
    String getApiKey();

    /**
     * Gets the base URL for the selected provider.
     * This is primarily used for Ollama, which requires a base URL.
     *
     * @return The base URL
     */
    String getBaseUrl();

    /**
     * Gets the maximum number of results for content retrieval.
     *
     * @return The maximum number of results
     */
    int getMaxResults();

    /**
     * Gets the minimum score for content retrieval.
     *
     * @return The minimum score
     */
    double getMinScore();

    /**
     * Gets the number of messages to keep in chat memory.
     *
     * @return The number of messages
     */
    int getChatMemoryMessages();

    /**
     * Gets the document chunk size.
     *
     * @return The chunk size
     */
    int getChunkSize();

    /**
     * Gets the document chunk overlap.
     *
     * @return The chunk overlap
     */
    int getChunkOverlap();

    /**
     * Checks if API requests should be logged.
     *
     * @return True if requests should be logged, false otherwise
     */
    boolean isLogRequests();

    /**
     * Checks if API responses should be logged.
     *
     * @return True if responses should be logged, false otherwise
     */
    boolean isLogResponses();
}
