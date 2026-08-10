package com.wikiknowledge.session.dto;

import com.wikiknowledge.domain.Session;

import java.time.OffsetDateTime;

public record SessionResponse(
        Long id,
        Long knowledgeBaseId,
        String title,
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
