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
@Table(name = "sessions")
@Getter
@Setter
/** 问答会话实体，对应 sessions 表。 */
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 会话主键 */
    private Long id;

    @Column(name = "user_id", nullable = false)
    /** 所属用户 ID */
    private Long userId;

    @Column(name = "knowledge_base_id", nullable = false)
    /** 会话使用的知识库 ID */
    private Long knowledgeBaseId;

    @Column(length = 200)
    /** 会话标题 */
    private String title;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    /** 创建时间 */
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    /** 最后更新时间 */
    private OffsetDateTime updatedAt;
}
