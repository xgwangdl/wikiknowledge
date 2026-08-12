package com.wikiknowledge.eval.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** 评估题目请求 DTO */
public record EvalQuestionRequest(
        /** 评估问题 */
        @NotBlank String question,
        /** 期望答案，供人工参考 */
        String expectedAnswer,
        /** 期望命中的 chunk id 列表 */
        List<Long> expectedChunkIds) {
}
