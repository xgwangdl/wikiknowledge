package com.wikiknowledge.session.dto;

import java.util.List;

public record SessionDetailResponse(
        SessionResponse session,
        List<MessageResponse> messages) {
}
