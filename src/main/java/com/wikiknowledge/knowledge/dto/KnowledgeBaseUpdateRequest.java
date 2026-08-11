package com.wikiknowledge.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KnowledgeBaseUpdateRequest(
        /** 知识库名称 */
        @NotBlank @Size(max = 100) String name,
        /** 知识库描述，选填 */
        @Size(max = 1000) String description) {
}
