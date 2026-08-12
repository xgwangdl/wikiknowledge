package com.wikiknowledge.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 聊天请求 DTO */
public record ChatRequest(
        /** 问答使用的知识库 ID */
        @NotNull Long knowledgeBaseId,
        /** 用户问题 */
        @NotBlank String question,
        /** 会话 ID；为空时系统自动创建新会话 */
        Long sessionId,
        /** 新会话标题，选填 */
        @Size(max = 100) String title) {
}
