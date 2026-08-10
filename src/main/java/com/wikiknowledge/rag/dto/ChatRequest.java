package com.wikiknowledge.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(
        @NotNull Long knowledgeBaseId,
        @NotBlank String question) {
}
