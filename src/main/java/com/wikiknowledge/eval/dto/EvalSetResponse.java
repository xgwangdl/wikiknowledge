package com.wikiknowledge.eval.dto;

import com.wikiknowledge.domain.EvalSet;

import java.time.OffsetDateTime;

public record EvalSetResponse(
        Long id,
        String name,
        String description,
        int questionCount,
        OffsetDateTime createdAt) {

    public static EvalSetResponse from(EvalSet evalSet, int questionCount) {
        return new EvalSetResponse(
                evalSet.getId(),
                evalSet.getName(),
                evalSet.getDescription(),
                questionCount,
                evalSet.getCreatedAt()
        );
    }
}
