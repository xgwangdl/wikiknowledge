package com.wikiknowledge.eval.dto;

import com.wikiknowledge.domain.EvalSet;

import java.time.OffsetDateTime;

/** 评估集响应 DTO */
public record EvalSetResponse(
        /** 评估集 ID */
        Long id,
        /** 评估集名称 */
        String name,
        /** 评估集描述 */
        String description,
        /** 题目数量 */
        int questionCount,
        /** 创建时间 */
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
