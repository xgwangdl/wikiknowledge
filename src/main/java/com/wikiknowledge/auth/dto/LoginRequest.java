package com.wikiknowledge.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** 登录请求 DTO */
public record LoginRequest(
        /** 用户名 */
        @NotBlank String username,
        /** 密码 */
        @NotBlank String password) {
}
