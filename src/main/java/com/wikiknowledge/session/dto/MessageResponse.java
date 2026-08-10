package com.wikiknowledge.session.dto;

import com.wikiknowledge.domain.Message;

import java.time.OffsetDateTime;

public record MessageResponse(
        Long id,
        String role,
        String content,
        String citations,
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
