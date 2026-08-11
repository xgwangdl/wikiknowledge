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
@Table(name = "eval_questions")
@Getter
@Setter
/** 评估题目实体，对应 eval_questions 表。 */
public class EvalQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 题目主键 */
    private Long id;

    @Column(name = "eval_set_id", nullable = false)
    /** 所属评估集 ID */
    private Long evalSetId;

    @Column(nullable = false)
    /** 评估问题 */
    private String question;

    @Column(name = "expected_answer")
    /** 期望答案，供人工参考 */
    private String expectedAnswer;

    @Column(name = "expected_chunk_ids")
    /** 期望命中的 chunk id，逗号分隔 */
    private String expectedChunkIds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    /** 创建时间 */
    private OffsetDateTime createdAt;
}
