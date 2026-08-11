package com.wikiknowledge.rag.dto;

import java.util.List;

public record SuggestionResponse(
        /** 推荐问题列表 */
        List<String> questions) {
}
