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
@Table(name = "users")
@Getter
@Setter
/** 系统用户实体，对应 users 表。 */
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 用户主键 */
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    /** 登录用户名，唯一 */
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    /** BCrypt 加密后的密码 */
    private String passwordHash;

    @Column(name = "display_name", length = 50)
    /** 用户显示名称 */
    private String displayName;

    @Column(nullable = false, length = 20)
    /** 角色：ROLE_USER 或 ROLE_ADMIN */
    private String role = "ROLE_USER";

    @Column(nullable = false, length = 20)
    /** 账号状态：ACTIVE 正常，DISABLED 禁用 */
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
