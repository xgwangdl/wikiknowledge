package com.wikiknowledge.auth.dto;

import com.wikiknowledge.domain.User;

/** 用户信息响应 DTO */
public record UserResponse(
        /** 用户 ID */
        Long id,
        /** 用户名 */
        String username,
        /** 显示名称 */
        String displayName,
        /** 角色：ROLE_USER 或 ROLE_ADMIN */
        String role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
    }
}
