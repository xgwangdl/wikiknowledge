package com.wikiknowledge.session.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 创建会话请求 DTO */
public record CreateSessionRequest(
        /** 会话使用的知识库 ID */
        @NotNull Long knowledgeBaseId,
        /** 会话标题，选填 */
        @Size(max = 100) String title) {
}
