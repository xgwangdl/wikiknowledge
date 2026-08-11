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
@Table(name = "knowledge_bases")
@Getter
@Setter
/** 知识库实体，对应 knowledge_bases 表。 */
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 知识库主键 */
    private Long id;

    @Column(nullable = false, length = 100)
    /** 知识库名称 */
    private String name;

    @Column(length = 1000)
    /** 知识库描述 */
    private String description;

    @Column(name = "owner_id")
    /** 创建人用户 ID，管理员可为空 */
    private Long ownerId;

    @Column(nullable = false, length = 20)
    /** 知识库状态：ACTIVE 正常 */
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    /** 创建时间 */
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    /** 最后更新时间 */
    private OffsetDateTime updatedAt;
}
