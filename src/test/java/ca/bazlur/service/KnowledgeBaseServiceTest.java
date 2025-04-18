package ca.bazlur.service;

import ca.bazlur.config.AIProvider;
import ca.bazlur.config.ConfigProvider;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KnowledgeBaseServiceTest {

  @Mock private ConfigProvider configProvider;

  @BeforeEach
  void setUp() {
    // Default to OpenAI provider for tests
    lenient().when(configProvider.getAIProvider()).thenReturn(AIProvider.OPENAI);
    lenient().when(configProvider.getApiKey()).thenReturn("test-api-key");
    lenient().when(configProvider.getBaseUrl()).thenReturn("http://localhost:11434");
    lenient().when(configProvider.getEmbeddingModelName()).thenReturn("test-embedding-model");
    lenient().when(configProvider.isLogRequests()).thenReturn(false);
    lenient().when(configProvider.isLogResponses()).thenReturn(false);
    lenient().when(configProvider.getChunkSize()).thenReturn(300);
    lenient().when(configProvider.getChunkOverlap()).thenReturn(30);
  }

  @Test
  void testConstructorWithDefaultResourceNames() {
    KnowledgeBaseService service = new KnowledgeBaseService(configProvider);
    assertNotNull(service);
  }

  @Test
  void testConstructorWithCustomResourceNames() {
    List<String> resourceNames = List.of("custom1.txt", "custom2.txt");
    KnowledgeBaseService service = new KnowledgeBaseService(configProvider, resourceNames);
    assertNotNull(service);
  }

  @Test
  void testLoadKnowledgeBaseWithMissingResource() {
    KnowledgeBaseService service =
        new KnowledgeBaseService(configProvider, List.of("non-existent.txt"));

    Exception exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              service.loadKnowledgeBase();
            });

    assertTrue(exception.getMessage().contains("Failed to find knowledge base documents"));
  }

  @Test
  void testLoadKnowledgeBaseSuccess() throws Exception {
    KnowledgeBaseService service =
        new KnowledgeBaseService(configProvider) {
          @Override
          protected InputStream getResourceAsStream(String resourceName) {
            return new ByteArrayInputStream(
                "Test content for resource: ".concat(resourceName).getBytes());
          }
        };

    // Use MockedStatic to mock the static builder methods
    try (MockedStatic<EmbeddingStoreIngestor> ingestorMock =
        mockStatic(EmbeddingStoreIngestor.class)) {
      EmbeddingStoreIngestor.Builder builder = mock(EmbeddingStoreIngestor.Builder.class);
      EmbeddingStoreIngestor ingestor = mock(EmbeddingStoreIngestor.class);

      ingestorMock.when(() -> EmbeddingStoreIngestor.builder()).thenReturn(builder);
      when(builder.documentSplitter(any())).thenReturn(builder);
      when(builder.embeddingModel(any())).thenReturn(builder);
      when(builder.embeddingStore(any())).thenReturn(builder);
      when(builder.build()).thenReturn(ingestor);

      when(ingestor.ingest(anyList())).thenReturn(mock(IngestionResult.class));

      EmbeddingStore<TextSegment> result = service.loadKnowledgeBase();
      assertNotNull(result);

      ingestorMock.verify(() -> EmbeddingStoreIngestor.builder());
      verify(builder).documentSplitter(any());
      verify(builder).embeddingModel(any());
      verify(builder).embeddingStore(any());
      verify(builder).build();
      verify(ingestor).ingest(anyList());
    }
  }
}
