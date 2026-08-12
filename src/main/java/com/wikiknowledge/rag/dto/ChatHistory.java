package com.wikiknowledge.rag.dto;

/** 单条对话历史。 */
public record ChatHistory(
        /** 消息角色：user 或 assistant */
        String role,
        /** 消息内容 */
        String content) {
}
