package com.wikiknowledge.eval;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikiknowledge.domain.EvalQuestion;
import com.wikiknowledge.domain.EvalResult;
import com.wikiknowledge.domain.EvalRun;
import com.wikiknowledge.repository.EvalQuestionRepository;
import com.wikiknowledge.repository.EvalResultRepository;
import com.wikiknowledge.repository.EvalRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvalReportServiceTest {

    @Mock
    private EvalRunRepository evalRunRepository;

    @Mock
    private EvalResultRepository evalResultRepository;

    @Mock
    private EvalQuestionRepository evalQuestionRepository;

    private EvalReportService evalReportService;

    @BeforeEach
    void setUp() {
        evalReportService = new EvalReportService(
                evalRunRepository,
                evalResultRepository,
                evalQuestionRepository,
                new ObjectMapper()
        );
    }

    @Test
    void exportsCsvWithQuestionsAndMetrics() {
        EvalRun run = new EvalRun();
        run.setId(1L);
        run.setMetrics("{\"avgRecall\":0.5,\"avgPrecision\":0.2,\"avgMrr\":1.0,\"questionCount\":1}");

        EvalResult result = new EvalResult();
        result.setQuestionId(10L);
        result.setRetrievedChunkIds("1,2");
        result.setRecall(0.5);
        result.setPrecision(0.2);
        result.setMrr(1.0);

        EvalQuestion question = new EvalQuestion();
        question.setId(10L);
        question.setQuestion("什么是 RAG？");
        question.setExpectedAnswer("检索增强生成");
        question.setExpectedChunkIds("1,2");

        when(evalRunRepository.findById(1L)).thenReturn(Optional.of(run));
        when(evalResultRepository.findByEvalRunIdOrderByIdAsc(1L)).thenReturn(List.of(result));
        when(evalQuestionRepository.findById(10L)).thenReturn(Optional.of(question));

        String csv = evalReportService.exportCsv(1L);

        assertThat(csv).contains("questionId,question,expectedAnswer");
        assertThat(csv).contains("什么是 RAG？");
        assertThat(csv).contains("avgRecall");
    }
}
