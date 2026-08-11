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
@Table(name = "eval_sets")
@Getter
@Setter
/** 黄金评估集实体，对应 eval_sets 表。 */
public class EvalSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 评估集主键 */
    private Long id;

    @Column(nullable = false, length = 100)
    /** 评估集名称 */
    private String name;

    /** 评估集描述 */
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    /** 创建时间 */
    private OffsetDateTime createdAt;
}
