package com.wikiknowledge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
/** 上传文档实体，对应 documents 表。 */
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 文档主键 */
    private Long id;

    @Column(name = "knowledge_base_id", nullable = false)
    /** 所属知识库 ID */
    private Long knowledgeBaseId;

    @Column(nullable = false, length = 255)
    /** 原始文件名 */
    private String filename;

    @Column(name = "file_hash", length = 64)
    /** 文件 SHA-256，用于同一知识库去重 */
    private String fileHash;

    @Column(name = "content_type", length = 100)
    /** 文件 MIME 类型 */
    private String contentType;

    @Column(name = "file_size")
    /** 文件大小（字节） */
    private Long fileSize;

    @Column(nullable = false, length = 20)
    /** 解析状态：UPLOADED/PARSING/INDEXING/READY/FAILED */
    private String status = "UPLOADED";

    @Column(name = "error_message")
    /** 解析失败时的错误信息 */
    private String errorMessage;

    @Column(name = "chunk_count", nullable = false)
    /** 生成的切片数量 */
    private Integer chunkCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    /** 创建时间 */
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    /** 最后更新时间 */
    private OffsetDateTime updatedAt;
}
