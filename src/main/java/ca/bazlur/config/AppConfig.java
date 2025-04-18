package ca.bazlur.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Application configuration provider that loads settings from application.properties.
 * Implements the ConfigProvider interface for dependency injection.
 */
public class AppConfig implements ConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private final Properties properties;

    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private static final String DEFAULT_CHAT_MODEL = "gpt-3.5-turbo";
    private static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small";
    private static final int DEFAULT_MAX_RESULTS = 3;
    private static final double DEFAULT_MIN_SCORE = 0.6;
    private static final int DEFAULT_CHAT_MEMORY_MESSAGES = 10;
    private static final int DEFAULT_CHUNK_SIZE = 300;
    private static final int DEFAULT_CHUNK_OVERLAP = 30;
    private static final String DEFAULT_API_KEY = "demo";

    // Singleton instance for backward compatibility
    private static final AppConfig INSTANCE = new AppConfig();

    /**
     * Creates a new AppConfig instance with the given properties.
     * This constructor is package-private for testing.
     *
     * @param properties The properties to use
     */
    AppConfig(Properties properties) {
        this.properties = properties;
    }

    /**
     * Creates a new AppConfig instance with properties loaded from application.properties.
     */
    private AppConfig() {
        this.properties = new Properties();
        loadProperties();
    }

    /**
     * Gets the singleton instance of AppConfig.
     * This method is provided for backward compatibility.
     *
     * @return The singleton instance
     */
    public static AppConfig getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new AppConfig instance with properties loaded from application.properties.
     * This factory method allows for creating new instances for testing.
     *
     * @return A new AppConfig instance
     */
    public static ConfigProvider create() {
        return new AppConfig();
    }

    /**
     * Loads properties from application.properties.
     */
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("Loaded configuration from application.properties");
            } else {
                logger.warn("application.properties not found, using default values and environment variables");
            }
        } catch (IOException e) {
            logger.error("Error loading application.properties, will rely on defaults and environment variables", e);
        }
    }

    @Override
    public String getChatModelName() {
        return getProperty("openai.chat.model", DEFAULT_CHAT_MODEL);
    }

    @Override
    public String getEmbeddingModelName() {
        return getProperty("openai.embedding.model", DEFAULT_EMBEDDING_MODEL);
    }

    @Override
    public String getApiKey() {
        String apiKey = getProperty("openai.api.key", DEFAULT_API_KEY);
        if (DEFAULT_API_KEY.equals(apiKey)) {
            logger.warn("Using default API key for OpenAI. Please set 'openai.api.key' in application.properties or the corresponding environment variable.");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("OpenAI API Key is missing or empty. Please set 'openai.api.key' in application.properties or the corresponding environment variable.");
            return null;
        }
        return apiKey;
    }

    @Override
    public int getMaxResults() {
        return getIntProperty("retriever.max.results", DEFAULT_MAX_RESULTS);
    }

    @Override
    public double getMinScore() {
        return getDoubleProperty("retriever.min.score", DEFAULT_MIN_SCORE);
    }

    @Override
    public int getChatMemoryMessages() {
        return getIntProperty("chat.memory.messages", DEFAULT_CHAT_MEMORY_MESSAGES);
    }

    @Override
    public int getChunkSize() {
        return getIntProperty("document.chunk.size", DEFAULT_CHUNK_SIZE);
    }

    @Override
    public int getChunkOverlap() {
        return getIntProperty("document.chunk.overlap", DEFAULT_CHUNK_OVERLAP);
    }

    @Override
    public boolean isLogRequests() {
        return getBooleanProperty("openai.log.requests", false);
    }

    @Override
    public boolean isLogResponses() {
        return getBooleanProperty("openai.log.responses", false);
    }

    /**
     * Resolves a value, checking for environment variable placeholders like ${VAR_NAME}.
     *
     * @param value The value read from properties (can be null).
     * @return The resolved value (from env var if placeholder found and env var exists),
     *         the original value if not a placeholder, or null if the placeholder's
     *         env var doesn't exist or the input value was null.
     */
    private String resolveValue(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = ENV_VAR_PATTERN.matcher(value);
        if (matcher.matches()) {
            String envVarName = matcher.group(1); // Get the content inside ${...}
            if (envVarName != null && !envVarName.isEmpty()) {
                String envVarValue = System.getenv(envVarName);
                if (envVarValue != null) {
                    logger.debug("Resolved property placeholder '{}' using environment variable '{}'", value, envVarName);
                    return envVarValue;
                } else {
                    logger.warn("Environment variable '{}' referenced in placeholder '{}' not found.", envVarName, value);
                    return null;
                }
            } else {
                logger.warn("Malformed environment variable placeholder found: {}", value);
                return null;
            }
        }
        return value;
    }

    /**
     * Gets a property value, resolving environment variable placeholders.
     *
     * @param key          The property key.
     * @param defaultValue The default value to return if the property is not found
     *                     or the placeholder cannot be resolved.
     * @return The resolved property value or the default value.
     */
    private String getProperty(String key, String defaultValue) {
        String rawValue = properties.getProperty(key);
        String resolvedValue = resolveValue(rawValue);
        return resolvedValue != null ? resolvedValue : defaultValue;
    }

    private int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key, null);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for key '{}': '{}' (from properties/env). Using default: {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }

    private double getDoubleProperty(String key, double defaultValue) {
        String value = getProperty(key, null);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid double value for key '{}': '{}' (from properties/env). Using default: {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }

    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key, null);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
}