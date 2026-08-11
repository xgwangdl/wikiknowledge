package com.wikiknowledge.document.dto;

import com.wikiknowledge.domain.Document;

import java.time.OffsetDateTime;

public record DocumentResponse(
        /** 文档 ID */
        Long id,
        /** 所属知识库 ID */
        Long knowledgeBaseId,
        /** 原始文件名 */
        String filename,
        /** 解析状态：UPLOADED/PARSING/INDEXING/READY/FAILED */
        String status,
        /** 解析失败时的错误信息 */
        String errorMessage,
        /** 切片数量 */
        Integer chunkCount,
        /** 创建时间 */
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
