package com.wikiknowledge.eval.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EvalSetCreateRequest(
        /** 评估集名称 */
        @NotBlank @Size(max = 100) String name,
        /** 评估集描述，选填 */
        String description,
        /** 评估题目列表 */
        @NotEmpty List<@Valid EvalQuestionRequest> questions) {
}
