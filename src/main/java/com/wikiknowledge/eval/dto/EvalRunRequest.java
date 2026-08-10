package com.wikiknowledge.eval.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EvalRunRequest(
        @NotNull Long evalSetId,
        @NotNull Long knowledgeBaseId,
        @Min(1) int topK) {
}
