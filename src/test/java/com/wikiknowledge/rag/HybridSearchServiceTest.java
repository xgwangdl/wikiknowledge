package com.wikiknowledge.rag;

import com.wikiknowledge.ai.EmbeddingService;
import com.wikiknowledge.repository.ChunkMatch;
import com.wikiknowledge.repository.ChunkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HybridSearchServiceTest {

    @Mock
    private ChunkRepository chunkRepository;

    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private HybridSearchService hybridSearchService;

    @Test
    void searchMergesVectorAndKeywordResults() {
        List<ChunkMatch> vectorMatches = List.of(chunkMatch(1L), chunkMatch(2L));
        List<ChunkMatch> keywordMatches = List.of(chunkMatch(2L), chunkMatch(3L));
        when(embeddingService.embed("问题")).thenReturn(new float[]{0.1f});
        when(embeddingService.toVectorLiteral(any())).thenReturn("[0.1]");
        when(chunkRepository.searchSimilar(eq(1L), eq("[0.1]"), eq(10)))
                .thenReturn(vectorMatches);
        when(chunkRepository.searchKeywords(eq(1L), eq("问题"), eq(10)))
                .thenReturn(keywordMatches);

        List<ChunkMatch> results = hybridSearchService.search(1L, "问题", 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(2L);
        assertThat(results.get(0).getSimilarity()).isEqualTo(0.8d);
        assertThat(results.get(1).getSimilarity()).isEqualTo(0.8d);
    }

    private ChunkMatch chunkMatch(Long id) {
        ChunkMatch match = mock(ChunkMatch.class);
        lenient().when(match.getId()).thenReturn(id);
        lenient().when(match.getDocumentId()).thenReturn(id);
        lenient().when(match.getKnowledgeBaseId()).thenReturn(1L);
        lenient().when(match.getContent()).thenReturn("内容" + id);
        lenient().when(match.getSeqNo()).thenReturn(id.intValue());
        lenient().when(match.getSimilarity()).thenReturn(0.8d);
        return match;
    }
}
