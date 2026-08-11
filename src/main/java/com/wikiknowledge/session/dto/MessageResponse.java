package com.wikiknowledge.session.dto;

import com.wikiknowledge.domain.Message;

import java.time.OffsetDateTime;

public record MessageResponse(
        /** 消息 ID */
        Long id,
        /** 消息角色：user 或 assistant */
        String role,
        /** 消息内容 */
        String content,
        /** AI 回答引用来源，JSON 文本 */
        String citations,
        /** 创建时间 */
        OffsetDateTime createdAt) {

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCitations(),
                message.getCreatedAt()
        );
    }
}
