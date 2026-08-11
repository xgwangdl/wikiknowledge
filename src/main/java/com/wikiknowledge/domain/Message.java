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
@Table(name = "messages")
@Getter
@Setter
/** 会话消息实体，对应 messages 表。 */
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 消息主键 */
    private Long id;

    @Column(name = "session_id", nullable = false)
    /** 所属会话 ID */
    private Long sessionId;

    @Column(nullable = false, length = 20)
    /** 消息角色：user 或 assistant */
    private String role;

    @Column(nullable = false)
    /** 消息内容 */
    private String content;

    @Column(columnDefinition = "text")
    /** AI 回答引用来源，JSON 文本 */
    private String citations;

    @Column(name = "tokens_in")
    /** 输入 token 数 */
    private Integer tokensIn;

    @Column(name = "tokens_out")
    /** 输出 token 数 */
    private Integer tokensOut;

    @Column(length = 20)
    /** 用户反馈：UP/DOWN */
    private String feedback;

    @Column(name = "feedback_reason", length = 500)
    /** 反馈原因 */
    private String feedbackReason;

    @Column(name = "latency_ms")
    /** 回答耗时（毫秒） */
    private Long latencyMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    /** 创建时间 */
    private OffsetDateTime createdAt;
}
