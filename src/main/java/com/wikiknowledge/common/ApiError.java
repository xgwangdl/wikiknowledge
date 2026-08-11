package com.wikiknowledge.common;

public record ApiError(
        /** HTTP 状态码 */
        int status,
        /** 业务错误码 */
        String code,
        /** 错误描述 */
        String message) {
}
