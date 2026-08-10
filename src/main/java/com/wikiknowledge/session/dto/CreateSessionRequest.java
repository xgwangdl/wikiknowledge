package com.wikiknowledge.session.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSessionRequest(
        @NotNull Long knowledgeBaseId,
        @Size(max = 100) String title) {
}
