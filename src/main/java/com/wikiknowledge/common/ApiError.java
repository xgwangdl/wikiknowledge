package com.wikiknowledge.common;

/** 统一错误响应结构 */
public record ApiError(
        /** HTTP 状态码 */
        int status,
        /** 业务错误码 */
        String code,
        /** 错误描述 */
        String message) {
}
