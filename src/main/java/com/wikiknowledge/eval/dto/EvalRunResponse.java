package com.wikiknowledge.eval.dto;

import com.wikiknowledge.domain.EvalRun;

import java.time.OffsetDateTime;

public record EvalRunResponse(
        Long id,
        Long evalSetId,
        String status,
        String metrics,
        String report,
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
