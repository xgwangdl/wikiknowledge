package com.wikiknowledge.repository;

/** 相似检索结果投影 */
public interface ChunkMatch {

    Long getId();

    Long getDocumentId();

    Long getKnowledgeBaseId();

    String getContent();

    Integer getSeqNo();

    Double getSimilarity();
}
