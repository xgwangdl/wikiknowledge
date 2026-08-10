package com.wikiknowledge.eval.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record EvalQuestionRequest(
        @NotBlank String question,
        String expectedAnswer,
        List<Long> expectedChunkIds) {
}
