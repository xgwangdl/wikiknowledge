package com.wikiknowledge.ai;

import com.wikiknowledge.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private ObjectProvider<EmbeddingModel> embeddingModelProvider;

    @Mock
    private EmbeddingModel embeddingModel;

    @InjectMocks
    private EmbeddingService embeddingService;

    @Test
    void embedReturnsVector() {
        when(embeddingModelProvider.getIfAvailable()).thenReturn(embeddingModel);
        when(embeddingModel.embed("问题")).thenReturn(new float[]{0.1f, 0.2f});

        assertThat(embeddingService.embed("问题")).containsExactly(0.1f, 0.2f);
    }

    @Test
    void toVectorLiteralFormatsFloatArray() {
        assertThat(embeddingService.toVectorLiteral(new float[]{0.1f, 0.2f})).isEqualTo("[0.1,0.2]");
    }

    @Test
    void throwsWhenAiDisabled() {
        when(embeddingModelProvider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> embeddingService.embed("问题"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("AI 服务未配置");
    }
}
