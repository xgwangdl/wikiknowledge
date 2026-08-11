package com.wikiknowledge.session.dto;

import java.util.List;

public record SessionDetailResponse(
        /** 会话信息 */
        SessionResponse session,
        /** 会话内的消息列表 */
        List<MessageResponse> messages) {
}
