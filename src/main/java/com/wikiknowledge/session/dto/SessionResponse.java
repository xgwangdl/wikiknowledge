package com.wikiknowledge.session.dto;

import com.wikiknowledge.domain.Session;

import java.time.OffsetDateTime;

/** 会话响应 DTO */
public record SessionResponse(
        /** 会话 ID */
        Long id,
        /** 会话使用的知识库 ID */
        Long knowledgeBaseId,
        /** 会话标题 */
        String title,
        /** 创建时间 */
        OffsetDateTime createdAt) {

    public static SessionResponse from(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getKnowledgeBaseId(),
                session.getTitle(),
                session.getCreatedAt()
        );
    }
}
