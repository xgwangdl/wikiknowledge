package com.wikiknowledge.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        /** 刷新令牌，用于换取新的 Access Token */
        @NotBlank String refreshToken) {
}
