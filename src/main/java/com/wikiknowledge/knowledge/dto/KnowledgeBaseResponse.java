package com.wikiknowledge.knowledge.dto;

import com.wikiknowledge.domain.KnowledgeBase;

import java.time.OffsetDateTime;

public record KnowledgeBaseResponse(
        /** 知识库 ID */
        Long id,
        /** 知识库名称 */
        String name,
        /** 知识库描述 */
        String description,
        /** 知识库状态：ACTIVE */
        String status,
        /** 创建人用户 ID */
        Long ownerId,
        /** 创建时间 */
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
