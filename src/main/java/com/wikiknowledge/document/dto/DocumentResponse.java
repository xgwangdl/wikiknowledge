package com.wikiknowledge.document.dto;

import com.wikiknowledge.domain.Document;

import java.time.OffsetDateTime;

public record DocumentResponse(
        Long id,
        Long knowledgeBaseId,
        String filename,
        String status,
        String errorMessage,
        Integer chunkCount,
        OffsetDateTime createdAt) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getKnowledgeBaseId(),
                document.getFilename(),
                document.getStatus(),
                document.getErrorMessage(),
                document.getChunkCount(),
                document.getCreatedAt()
        );
    }
}
