package com.wikiknowledge.eval.dto;

import java.util.List;

public record EvalQuestionResponse(
        Long id,
        String question,
        String expectedAnswer,
        List<Long> expectedChunkIds) {
}
