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

import java.time.OffsetDateTime;

@Entity
@Table(name = "chunks")
@Getter
@Setter
/** 文档切片实体，对应 chunks 表，保存文本块与向量。 */
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 切片主键 */
    private Long id;

    @Column(name = "document_id", nullable = false)
    /** 来源文档 ID */
    private Long documentId;

    @Column(name = "knowledge_base_id", nullable = false)
    /** 所属知识库 ID */
    private Long knowledgeBaseId;

    @Column(nullable = false)
    /** 切片文本内容 */
    private String content;

    @Column(name = "token_count")
    /** 估算的 token 数 */
    private Integer tokenCount;

    @Column(name = "seq_no", nullable = false)
    /** 切片在文档中的序号 */
    private Integer seqNo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    /** 创建时间 */
    private OffsetDateTime createdAt;
}
