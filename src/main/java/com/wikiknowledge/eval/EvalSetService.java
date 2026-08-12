package com.wikiknowledge.eval;

import com.wikiknowledge.domain.EvalQuestion;
import com.wikiknowledge.domain.EvalSet;
import com.wikiknowledge.eval.dto.EvalQuestionResponse;
import com.wikiknowledge.eval.dto.EvalSetCreateRequest;
import com.wikiknowledge.eval.dto.EvalSetDetailResponse;
import com.wikiknowledge.eval.dto.EvalSetResponse;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.repository.EvalQuestionRepository;
import com.wikiknowledge.repository.EvalSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;/** 评估集管理业务逻辑 */


@Service
public class EvalSetService {

    private final EvalSetRepository evalSetRepository;
    private final EvalQuestionRepository evalQuestionRepository;

    public EvalSetService(EvalSetRepository evalSetRepository,
                          EvalQuestionRepository evalQuestionRepository) {
        this.evalSetRepository = evalSetRepository;
        this.evalQuestionRepository = evalQuestionRepository;
    }

    /**
     * 创建评估集并批量保存评估题目。
     *
     * @param request 创建评估集请求
     * @return 创建后的评估集信息
     */
    @Transactional
    public EvalSetResponse create(EvalSetCreateRequest request) {
        EvalSet evalSet = new EvalSet();
        evalSet.setName(request.name());
        evalSet.setDescription(request.description());
        evalSet = evalSetRepository.save(evalSet);

        for (var questionRequest : request.questions()) {
            EvalQuestion question = new EvalQuestion();
            question.setEvalSetId(evalSet.getId());
            question.setQuestion(questionRequest.question());
            question.setExpectedAnswer(questionRequest.expectedAnswer());
            question.setExpectedChunkIds(toCsv(questionRequest.expectedChunkIds()));
            evalQuestionRepository.save(question);
        }
        return EvalSetResponse.from(evalSet, request.questions().size());
    }

    @Transactional(readOnly = true)
    public List<EvalSetResponse> list() {
        return evalSetRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(evalSet -> EvalSetResponse.from(
                        evalSet,
                        evalQuestionRepository.findByEvalSetIdOrderByIdAsc(evalSet.getId()).size()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public EvalSetDetailResponse get(Long id) {
        EvalSet evalSet = findEvalSet(id);
        List<EvalQuestionResponse> questions = evalQuestionRepository
                .findByEvalSetIdOrderByIdAsc(id)
                .stream()
                .map(this::toResponse)
                .toList();
        return new EvalSetDetailResponse(
                EvalSetResponse.from(evalSet, questions.size()),
                questions
        );
    }

    @Transactional
    public void delete(Long id) {
        evalSetRepository.delete(findEvalSet(id));
    }

    @Transactional(readOnly = true)
    public EvalSet findEvalSet(Long id) {
        return evalSetRepository.findById(id)
                .orElseThrow(() -> new BusinessException("EVAL_SET_NOT_FOUND", "评估集不存在"));
    }

    private EvalQuestionResponse toResponse(EvalQuestion question) {
        return new EvalQuestionResponse(
                question.getId(),
                question.getQuestion(),
                question.getExpectedAnswer(),
                parseIds(question.getExpectedChunkIds())
        );
    }

    private String toCsv(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private List<Long> parseIds(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split("[,]"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Long::valueOf)
                .toList();
    }
}
