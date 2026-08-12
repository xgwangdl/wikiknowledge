package com.wikiknowledge.session.dto;

import java.util.List;

/** 会话详情响应 DTO */
public record SessionDetailResponse(
        /** 会话信息 */
        SessionResponse session,
        /** 会话内的消息列表 */
        List<MessageResponse> messages) {
}
