package com.wikiknowledge.rag.dto;

import java.util.List;

/** 问题建议响应 DTO */
public record SuggestionResponse(
        /** 推荐问题列表 */
        List<String> questions) {
}
