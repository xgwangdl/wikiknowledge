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
@Table(name = "eval_results")
@Getter
@Setter
/** 评估题目检索结果实体，对应 eval_results 表。 */
public class EvalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 结果主键 */
    private Long id;

    @Column(name = "eval_run_id", nullable = false)
    /** 所属评估运行 ID */
    private Long evalRunId;

    @Column(name = "question_id", nullable = false)
    /** 评估题目 ID */
    private Long questionId;

    @Column(name = "retrieved_chunk_ids")
    /** 实际检索到的 chunk id，逗号分隔 */
    private String retrievedChunkIds;

    /** Recall@k：命中的期望结果比例 */
    private Double recall;

    /** Precision@k：检索结果中命中比例 */
    private Double precision;

    /** MRR：第一个命中结果的倒数排名 */
    private Double mrr;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    /** 创建时间 */
    private OffsetDateTime createdAt;
}
