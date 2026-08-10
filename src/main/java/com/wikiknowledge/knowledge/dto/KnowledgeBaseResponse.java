package com.wikiknowledge.knowledge.dto;

import com.wikiknowledge.domain.KnowledgeBase;

import java.time.OffsetDateTime;

public record KnowledgeBaseResponse(
        Long id,
        String name,
        String description,
        String status,
        Long ownerId,
        OffsetDateTime createdAt) {

    public static KnowledgeBaseResponse from(KnowledgeBase knowledgeBase) {
        return new KnowledgeBaseResponse(
                knowledgeBase.getId(),
                knowledgeBase.getName(),
                knowledgeBase.getDescription(),
                knowledgeBase.getStatus(),
                knowledgeBase.getOwnerId(),
                knowledgeBase.getCreatedAt()
        );
    }
}
