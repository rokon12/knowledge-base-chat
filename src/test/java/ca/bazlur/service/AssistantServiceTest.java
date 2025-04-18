package ca.bazlur.service;

import ca.bazlur.config.ConfigProvider;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {

    @Mock
    private ConfigProvider configProvider;

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;


    @Test
    void testServiceCreation() {
        AssistantService service = new AssistantService(configProvider, embeddingStore) {
            @Override
            protected void initialize() {
            }
        };

        assertNotNull(service);
    }

    @Test
    void testProcessMessage() throws Exception {
        // Create a test subclass that doesn't actually initialize the AI components
        AssistantService service = new AssistantService(configProvider, embeddingStore) {
            @Override
            protected void initialize() {
            }
        };

        AssistantService.Assistant mockAssistant = mock(AssistantService.Assistant.class);
        when(mockAssistant.chat("test message")).thenReturn("test response");

        Field assistantField = AssistantService.class.getDeclaredField("assistant");
        assistantField.setAccessible(true);
        assistantField.set(service, mockAssistant);

        String response = service.processMessage("test message");

        assertEquals("test response", response);

        verify(mockAssistant).chat("test message");
    }
}