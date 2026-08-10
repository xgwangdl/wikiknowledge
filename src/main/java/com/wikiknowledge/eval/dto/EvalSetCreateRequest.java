package com.wikiknowledge.eval.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EvalSetCreateRequest(
        @NotBlank @Size(max = 100) String name,
        String description,
        @NotEmpty List<@Valid EvalQuestionRequest> questions) {
}
