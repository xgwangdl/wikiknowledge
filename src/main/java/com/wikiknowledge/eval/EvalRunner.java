package com.wikiknowledge.eval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikiknowledge.ai.EmbeddingService;
import com.wikiknowledge.domain.EvalQuestion;
import com.wikiknowledge.domain.EvalResult;
import com.wikiknowledge.domain.EvalRun;
import com.wikiknowledge.eval.dto.EvalRunRequest;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.repository.ChunkMatch;
import com.wikiknowledge.repository.ChunkRepository;
import com.wikiknowledge.repository.EvalQuestionRepository;
import com.wikiknowledge.repository.EvalResultRepository;
import com.wikiknowledge.repository.EvalRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;/** 评估执行器：计算 Recall/Precision/MRR */


@Service
public class EvalRunner {

    private final EvalQuestionRepository evalQuestionRepository;
    private final EvalResultRepository evalResultRepository;
    private final EvalRunRepository evalRunRepository;
    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    public EvalRunner(EvalQuestionRepository evalQuestionRepository,
                      EvalResultRepository evalResultRepository,
                      EvalRunRepository evalRunRepository,
                      ChunkRepository chunkRepository,
                      EmbeddingService embeddingService,
                      ObjectMapper objectMapper) {
        this.evalQuestionRepository = evalQuestionRepository;
        this.evalResultRepository = evalResultRepository;
        this.evalRunRepository = evalRunRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行评估：逐题向量检索，计算 Recall/Precision/MRR，最后写聚合指标。
     */
    @Transactional
    public EvalRun run(EvalRunRequest request) {
        // 1. 加载评估题目
        List<EvalQuestion> questions = evalQuestionRepository.findByEvalSetIdOrderByIdAsc(request.evalSetId());
        if (questions.isEmpty()) {
            throw new BusinessException("EMPTY_EVAL_SET", "评估集没有题目");
        }

        // 2. 创建运行记录
        EvalRun evalRun = new EvalRun();
        evalRun.setEvalSetId(request.evalSetId());
        evalRun.setStatus("RUNNING");
        evalRun = evalRunRepository.save(evalRun);

        List<Double> recalls = new ArrayList<>();
        List<Double> precisions = new ArrayList<>();
        List<Double> mrrs = new ArrayList<>();

        try {
            // 3. 逐题检索并计算指标
            for (EvalQuestion question : questions) {
                float[] vector = embeddingService.embed(question.getQuestion());
                List<ChunkMatch> retrieved = chunkRepository.searchSimilar(
                        request.knowledgeBaseId(),
                        embeddingService.toVectorLiteral(vector),
                        request.topK()
                );
                Set<Long> expected = parseExpected(question.getExpectedChunkIds());
                Set<Long> retrievedIds = retrieved.stream()
                        .map(ChunkMatch::getId)
                        .collect(Collectors.toSet());

                long hits = expected.stream().filter(retrievedIds::contains).count();
                double recall = expected.isEmpty() ? 0.0 : (double) hits / expected.size();
                double precision = (double) hits / request.topK();
                double mrr = computeMrr(retrieved, expected);

                recalls.add(recall);
                precisions.add(precision);
                mrrs.add(mrr);

                EvalResult result = new EvalResult();
                result.setEvalRunId(evalRun.getId());
                result.setQuestionId(question.getId());
                result.setRetrievedChunkIds(retrieved.stream()
                        .map(match -> String.valueOf(match.getId()))
                        .collect(Collectors.joining(",")));
                result.setRecall(recall);
                result.setPrecision(precision);
                result.setMrr(mrr);
                evalResultRepository.save(result);
            }

            // 4. 计算聚合指标
            Map<String, Object> metrics = Map.of(
                    "questionCount", questions.size(),
                    "avgRecall", average(recalls),
                    "avgPrecision", average(precisions),
                    "avgMrr", average(mrrs)
            );
            evalRun.setMetrics(objectMapper.writeValueAsString(metrics));
            evalRun.setStatus("COMPLETED");
        } catch (Exception ex) {
            evalRun.setStatus("FAILED");
            evalRun.setReport(ex.getMessage());
        }
        return evalRunRepository.save(evalRun);
    }

    /**
     * 解析期望命中的 chunk id 字符串为集合。
     */
    private Set<Long> parseExpected(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        Set<Long> ids = new HashSet<>();
        for (String part : csv.split("[,]")) {
            String value = part.trim();
            if (!value.isEmpty()) {
                ids.add(Long.valueOf(value));
            }
        }
        return ids;
    }

    /**
     * 计算 MRR：第一个命中结果的倒数排名。
     */
    private double computeMrr(List<ChunkMatch> retrieved, Set<Long> expected) {
        if (expected.isEmpty()) {
            return 0.0;
        }
        for (int i = 0; i < retrieved.size(); i++) {
            if (expected.contains(retrieved.get(i).getId())) {
                return 1.0 / (i + 1);
            }
        }
        return 0.0;
    }

    private double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
