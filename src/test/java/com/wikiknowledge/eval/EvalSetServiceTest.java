package com.wikiknowledge.eval;

import com.wikiknowledge.domain.EvalQuestion;
import com.wikiknowledge.domain.EvalSet;
import com.wikiknowledge.eval.dto.EvalQuestionRequest;
import com.wikiknowledge.eval.dto.EvalSetCreateRequest;
import com.wikiknowledge.eval.dto.EvalSetResponse;
import com.wikiknowledge.repository.EvalQuestionRepository;
import com.wikiknowledge.repository.EvalSetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvalSetServiceTest {

    @Mock
    private EvalSetRepository evalSetRepository;

    @Mock
    private EvalQuestionRepository evalQuestionRepository;

    @InjectMocks
    private EvalSetService evalSetService;

    @Test
    void createSavesSetAndQuestions() {
        when(evalSetRepository.save(any(EvalSet.class))).thenAnswer(invocation -> {
            EvalSet evalSet = invocation.getArgument(0);
            evalSet.setId(1L);
            return evalSet;
        });

        EvalSetCreateRequest request = new EvalSetCreateRequest(
                "基础评估集",
                "测试",
                List.of(new EvalQuestionRequest("什么是 RAG？", "Retrieval-Augmented Generation", List.of(1L, 2L)))
        );

        EvalSetResponse response = evalSetService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.questionCount()).isEqualTo(1);
        verify(evalQuestionRepository).save(any(EvalQuestion.class));
    }

    @Test
    void listReturnsSetsWithQuestionCount() {
        EvalSet evalSet = new EvalSet();
        evalSet.setId(1L);
        evalSet.setName("评估集");
        when(evalSetRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(evalSet));
        when(evalQuestionRepository.findByEvalSetIdOrderByIdAsc(1L)).thenReturn(List.of(new EvalQuestion()));

        List<EvalSetResponse> responses = evalSetService.list();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).questionCount()).isEqualTo(1);
    }
}
