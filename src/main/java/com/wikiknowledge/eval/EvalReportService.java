package com.wikiknowledge.eval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikiknowledge.domain.EvalQuestion;
import com.wikiknowledge.domain.EvalResult;
import com.wikiknowledge.domain.EvalRun;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.repository.EvalQuestionRepository;
import com.wikiknowledge.repository.EvalResultRepository;
import com.wikiknowledge.repository.EvalRunRepository;
import org.springframework.stereotype.Service;

import java.util.List;/** 评估报告 CSV 导出服务 */


@Service
public class EvalReportService {

    private final EvalRunRepository evalRunRepository;
    private final EvalResultRepository evalResultRepository;
    private final EvalQuestionRepository evalQuestionRepository;
    private final ObjectMapper objectMapper;

    public EvalReportService(EvalRunRepository evalRunRepository,
                             EvalResultRepository evalResultRepository,
                             EvalQuestionRepository evalQuestionRepository,
                             ObjectMapper objectMapper) {
        this.evalRunRepository = evalRunRepository;
        this.evalResultRepository = evalResultRepository;
        this.evalQuestionRepository = evalQuestionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 导出评估运行报告为 CSV，包含每题指标与聚合指标。
     *
     * @param runId 评估运行 ID
     * @return CSV 文本内容
     */
    public String exportCsv(Long runId) {
        EvalRun run = evalRunRepository.findById(runId)
                .orElseThrow(() -> new BusinessException("EVAL_RUN_NOT_FOUND", "评估运行不存在"));
        List<EvalResult> results = evalResultRepository.findByEvalRunIdOrderByIdAsc(runId);

        // 添加 BOM，避免 Excel 打开中文乱码
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("questionId,question,expectedAnswer,expectedChunkIds,retrievedChunkIds,recall,precision,mrr\n");

        for (EvalResult result : results) {
            EvalQuestion question = evalQuestionRepository.findById(result.getQuestionId()).orElse(null);
            csv.append(escape(result.getQuestionId())).append(',')
                    .append(escape(question == null ? "" : question.getQuestion())).append(',')
                    .append(escape(question == null ? "" : question.getExpectedAnswer())).append(',')
                    .append(escape(question == null ? "" : question.getExpectedChunkIds())).append(',')
                    .append(escape(result.getRetrievedChunkIds())).append(',')
                    .append(escape(result.getRecall())).append(',')
                    .append(escape(result.getPrecision())).append(',')
                    .append(escape(result.getMrr())).append('\n');
        }

        appendSummary(csv, run.getMetrics());
        return csv.toString();
    }

    private void appendSummary(StringBuilder csv, String metrics) {
        try {
            JsonNode node = objectMapper.readTree(metrics);
            csv.append("summary,avgRecall,avgPrecision,avgMrr,questionCount\n");
            csv.append(',').append(escape(node.path("avgRecall").asText()))
                    .append(',').append(escape(node.path("avgPrecision").asText()))
                    .append(',').append(escape(node.path("avgMrr").asText()))
                    .append(',').append(escape(node.path("questionCount").asText()))
                    .append('\n');
        } catch (Exception ignored) {
            // metrics 为空或解析失败时忽略汇总行
        }
    }

    /**
     * CSV 转义：包含逗号、引号或换行时用引号包裹并双写引号。
     *
     * @param value 原始字段值
     * @return 转义后的 CSV 字段
     */
    private String escape(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
