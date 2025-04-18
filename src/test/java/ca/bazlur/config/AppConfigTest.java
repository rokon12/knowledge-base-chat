package ca.bazlur.config;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AppConfig class.
 */
class AppConfigTest {

    @Test
    void testDefaultValues() {
        // Create an AppConfig with empty properties
        Properties properties = new Properties();
        AppConfig config = new AppConfig(properties);

        // Verify default values
        assertEquals(AIProvider.OPENAI, config.getAIProvider());
        assertEquals("gpt-3.5-turbo", config.getChatModelName());
        assertEquals("text-embedding-3-small", config.getEmbeddingModelName());
        assertEquals("demo", config.getApiKey());
        assertNull(config.getBaseUrl()); // OpenAI doesn't use baseUrl
        assertEquals(3, config.getMaxResults());
        assertEquals(0.6, config.getMinScore());
        assertEquals(10, config.getChatMemoryMessages());
        assertEquals(300, config.getChunkSize());
        assertEquals(30, config.getChunkOverlap());
        assertFalse(config.isLogRequests());
        assertFalse(config.isLogResponses());
    }

    @Test
    void testCustomValuesOpenAI() {
        // Create properties with custom values for OpenAI
        Properties properties = new Properties();
        properties.setProperty("ai.provider", "OPENAI");
        properties.setProperty("openai.chat.model", "gpt-4");
        properties.setProperty("openai.embedding.model", "text-embedding-ada-002");
        properties.setProperty("openai.api.key", "test-api-key");
        properties.setProperty("retriever.max.results", "5");
        properties.setProperty("retriever.min.score", "0.8");
        properties.setProperty("chat.memory.messages", "20");
        properties.setProperty("document.chunk.size", "500");
        properties.setProperty("document.chunk.overlap", "50");
        properties.setProperty("openai.log.requests", "true");
        properties.setProperty("openai.log.responses", "true");

        AppConfig config = new AppConfig(properties);

        // Verify custom values
        assertEquals(AIProvider.OPENAI, config.getAIProvider());
        assertEquals("gpt-4", config.getChatModelName());
        assertEquals("text-embedding-ada-002", config.getEmbeddingModelName());
        assertEquals("test-api-key", config.getApiKey());
        assertNull(config.getBaseUrl()); // OpenAI doesn't use baseUrl
        assertEquals(5, config.getMaxResults());
        assertEquals(0.8, config.getMinScore());
        assertEquals(20, config.getChatMemoryMessages());
        assertEquals(500, config.getChunkSize());
        assertEquals(50, config.getChunkOverlap());
        assertTrue(config.isLogRequests());
        assertTrue(config.isLogResponses());
    }

    @Test
    void testCustomValuesOllama() {
        // Create properties with custom values for Ollama
        Properties properties = new Properties();
        properties.setProperty("ai.provider", "OLLAMA");
        properties.setProperty("ollama.chat.model", "llama2");
        properties.setProperty("ollama.embedding.model", "nomic-embed-text");
        properties.setProperty("ollama.base.url", "http://localhost:12345");
        properties.setProperty("retriever.max.results", "5");
        properties.setProperty("retriever.min.score", "0.8");
        properties.setProperty("chat.memory.messages", "20");
        properties.setProperty("document.chunk.size", "500");
        properties.setProperty("document.chunk.overlap", "50");
        properties.setProperty("ollama.log.requests", "true");
        properties.setProperty("ollama.log.responses", "true");

        AppConfig config = new AppConfig(properties);

        // Verify custom values
        assertEquals(AIProvider.OLLAMA, config.getAIProvider());
        assertEquals("llama2", config.getChatModelName());
        assertEquals("nomic-embed-text", config.getEmbeddingModelName());
        assertNull(config.getApiKey()); // Ollama doesn't use apiKey
        assertEquals("http://localhost:12345", config.getBaseUrl());
        assertEquals(5, config.getMaxResults());
        assertEquals(0.8, config.getMinScore());
        assertEquals(20, config.getChatMemoryMessages());
        assertEquals(500, config.getChunkSize());
        assertEquals(50, config.getChunkOverlap());
        assertTrue(config.isLogRequests());
        assertTrue(config.isLogResponses());
    }

    @Test
    void testInvalidNumericValues() {
        // Create properties with invalid numeric values
        Properties properties = new Properties();
        properties.setProperty("retriever.max.results", "invalid");
        properties.setProperty("retriever.min.score", "invalid");
        properties.setProperty("chat.memory.messages", "invalid");
        properties.setProperty("document.chunk.size", "invalid");
        properties.setProperty("document.chunk.overlap", "invalid");

        AppConfig config = new AppConfig(properties);

        // Verify default values are used for invalid inputs
        assertEquals(3, config.getMaxResults());
        assertEquals(0.6, config.getMinScore());
        assertEquals(10, config.getChatMemoryMessages());
        assertEquals(300, config.getChunkSize());
        assertEquals(30, config.getChunkOverlap());
    }

    @Test
    void testFactoryMethod() {
        // Verify that the factory method returns a ConfigProvider
        ConfigProvider config = AppConfig.create();
        assertNotNull(config);
        assertTrue(config instanceof AppConfig);
    }

    @Test
    void testSingleton() {
        // Verify that getInstance returns the same instance
        AppConfig instance1 = AppConfig.getInstance();
        AppConfig instance2 = AppConfig.getInstance();
        assertSame(instance1, instance2);
    }
}
