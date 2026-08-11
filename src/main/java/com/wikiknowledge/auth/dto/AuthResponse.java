package com.wikiknowledge.auth.dto;

public record AuthResponse(
        /** 短期 Access Token，用于接口鉴权 */
        String accessToken,
        /** 长期 Refresh Token，用于刷新 Access Token */
        String refreshToken,
        /** 当前登录用户信息 */
        UserResponse user) {
}
