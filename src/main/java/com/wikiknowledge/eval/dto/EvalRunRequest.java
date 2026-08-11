package com.wikiknowledge.eval.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EvalRunRequest(
        /** 评估集 ID */
        @NotNull Long evalSetId,
        /** 用于评估检索的知识库 ID */
        @NotNull Long knowledgeBaseId,
        /** 检索返回的 topK 数量 */
        @Min(1) int topK) {
}
