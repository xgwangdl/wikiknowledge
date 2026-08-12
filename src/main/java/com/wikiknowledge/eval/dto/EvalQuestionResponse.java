package com.wikiknowledge.eval.dto;

import java.util.List;

/** 评估题目响应 DTO */
public record EvalQuestionResponse(
        /** 题目 ID */
        Long id,
        /** 评估问题 */
        String question,
        /** 期望答案 */
        String expectedAnswer,
        /** 期望命中的 chunk id 列表 */
        List<Long> expectedChunkIds) {
}
