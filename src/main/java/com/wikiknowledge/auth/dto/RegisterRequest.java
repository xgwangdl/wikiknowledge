package com.wikiknowledge.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        /** 注册用户名，3-50 个字符 */
        @NotBlank @Size(min = 3, max = 50) String username,
        /** 注册密码，至少 6 位 */
        @NotBlank @Size(min = 6, max = 64) String password,
        /** 用户显示名称，选填 */
        @Size(max = 50) String displayName) {
}
