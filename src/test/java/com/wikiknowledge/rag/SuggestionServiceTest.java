package com.wikiknowledge.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikiknowledge.ai.EmbeddingService;
import com.wikiknowledge.domain.Chunk;
import com.wikiknowledge.domain.KnowledgeBase;
import com.wikiknowledge.repository.ChunkRepository;
import com.wikiknowledge.repository.KnowledgeBaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Mock
    private ChunkRepository chunkRepository;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private ObjectProvider<ChatModel> chatModelProvider;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private SuggestionService suggestionService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        suggestionService = new SuggestionService(
                knowledgeBaseRepository,
                chunkRepository,
                embeddingService,
                chatModelProvider,
                redisTemplate,
                new ObjectMapper()
        );
    }

    @Test
    void returnsDefaultSuggestionsWhenNoChunks() {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(1L);
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(chunkRepository.findTop3ByKnowledgeBaseIdOrderByIdAsc(1L)).thenReturn(List.of());

        List<String> suggestions = suggestionService.suggest(1L, null);

        assertThat(suggestions).hasSize(3);
        assertThat(suggestions.get(0)).contains("知识库");
    }

    @Test
    void returnsCachedSuggestions() throws Exception {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(1L);
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(valueOperations.get(anyString())).thenReturn("[\"缓存问题一\"]");

        List<String> suggestions = suggestionService.suggest(1L, "测试");

        assertThat(suggestions).containsExactly("缓存问题一");
    }

    @Test
    void returnsFallbackSuggestionsFromChunks() {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(1L);
        Chunk chunk = new Chunk();
        chunk.setContent("维基知识库支持文档上传、切片和向量检索");
        when(knowledgeBaseRepository.findById(1L)).thenReturn(Optional.of(kb));
        when(chunkRepository.findTop3ByKnowledgeBaseIdOrderByIdAsc(1L)).thenReturn(List.of(chunk));

        List<String> suggestions = suggestionService.suggest(1L, null);

        assertThat(suggestions.get(0)).contains("可以问什么");
    }
}
