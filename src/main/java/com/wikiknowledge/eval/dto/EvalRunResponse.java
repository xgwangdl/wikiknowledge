package com.wikiknowledge.eval.dto;

import com.wikiknowledge.domain.EvalRun;

import java.time.OffsetDateTime;

public record EvalRunResponse(
        /** 评估运行 ID */
        Long id,
        /** 使用的评估集 ID */
        Long evalSetId,
        /** 运行状态：RUNNING/COMPLETED/FAILED */
        String status,
        /** 聚合指标 JSON：Recall/Precision/MRR */
        String metrics,
        /** 失败原因或详细报告 */
        String report,
        /** 创建时间 */
        OffsetDateTime createdAt) {

    public static EvalRunResponse from(EvalRun evalRun) {
        return new EvalRunResponse(
                evalRun.getId(),
                evalRun.getEvalSetId(),
                evalRun.getStatus(),
                evalRun.getMetrics(),
                evalRun.getReport(),
                evalRun.getCreatedAt()
        );
    }
}
