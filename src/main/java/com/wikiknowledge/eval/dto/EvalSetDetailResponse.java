package com.wikiknowledge.eval.dto;

import java.util.List;

public record EvalSetDetailResponse(
        EvalSetResponse set,
        List<EvalQuestionResponse> questions) {
}
