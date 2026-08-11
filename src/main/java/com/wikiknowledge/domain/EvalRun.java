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
@Table(name = "eval_runs")
@Getter
@Setter
/** 评估运行记录实体，对应 eval_runs 表。 */
public class EvalRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 运行主键 */
    private Long id;

    @Column(name = "eval_set_id", nullable = false)
    /** 使用的评估集 ID */
    private Long evalSetId;

    @Column(nullable = false, length = 20)
    /** 运行状态：RUNNING/COMPLETED/FAILED */
    private String status = "RUNNING";

    /** 聚合指标 JSON：Recall、Precision、MRR */
    private String metrics;

    /** 失败原因或详细报告文本 */
    private String report;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    /** 创建时间 */
    private OffsetDateTime createdAt;
}
