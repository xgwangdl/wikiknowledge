package com.wikiknowledge.eval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikiknowledge.ai.EmbeddingService;
import com.wikiknowledge.domain.EvalQuestion;
import com.wikiknowledge.domain.EvalResult;
import com.wikiknowledge.domain.EvalRun;
import com.wikiknowledge.eval.dto.EvalRunRequest;
import com.wikiknowledge.repository.ChunkMatch;
import com.wikiknowledge.repository.ChunkRepository;
import com.wikiknowledge.repository.EvalQuestionRepository;
import com.wikiknowledge.repository.EvalResultRepository;
import com.wikiknowledge.repository.EvalRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvalRunnerTest {

    @Mock
    private EvalQuestionRepository evalQuestionRepository;

    @Mock
    private EvalResultRepository evalResultRepository;

    @Mock
    private EvalRunRepository evalRunRepository;

    @Mock
    private ChunkRepository chunkRepository;

    @Mock
    private EmbeddingService embeddingService;

    private EvalRunner evalRunner;

    @BeforeEach
    void setUp() {
        evalRunner = new EvalRunner(
                evalQuestionRepository,
                evalResultRepository,
                evalRunRepository,
                chunkRepository,
                embeddingService,
                new ObjectMapper()
        );
    }

    @Test
    void runComputesRetrievalMetrics() throws Exception {
        EvalQuestion question = new EvalQuestion();
        question.setId(1L);
        question.setQuestion("什么是 RAG？");
        question.setExpectedChunkIds("11,12");
        when(evalQuestionRepository.findByEvalSetIdOrderByIdAsc(1L)).thenReturn(List.of(question));
        when(evalRunRepository.save(any(EvalRun.class))).thenAnswer(invocation -> {
            EvalRun evalRun = invocation.getArgument(0);
            if (evalRun.getId() == null) {
                evalRun.setId(10L);
            }
            return evalRun;
        });
        when(embeddingService.embed("什么是 RAG？")).thenReturn(new float[]{0.1f});
        when(embeddingService.toVectorLiteral(any())).thenReturn("[0.1]");

        ChunkMatch hit = chunkMatch(11L);
        ChunkMatch miss = chunkMatch(13L);
        when(chunkRepository.searchSimilar(eq(1L), eq("[0.1]"), eq(10))).thenReturn(List.of(hit, miss));
        when(evalResultRepository.save(any(EvalResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvalRun run = evalRunner.run(new EvalRunRequest(1L, 1L, 10));

        assertThat(run.getStatus()).isEqualTo("COMPLETED");
        assertThat(run.getMetrics()).contains("avgRecall");
        verify(evalResultRepository).save(any(EvalResult.class));
    }

    private ChunkMatch chunkMatch(Long id) {
        ChunkMatch match = mock(ChunkMatch.class);
        lenient().when(match.getId()).thenReturn(id);
        return match;
    }
}
