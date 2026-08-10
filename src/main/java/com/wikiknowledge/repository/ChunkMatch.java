package com.wikiknowledge.repository;

public interface ChunkMatch {

    Long getId();

    Long getDocumentId();

    Long getKnowledgeBaseId();

    String getContent();

    Integer getSeqNo();

    Double getSimilarity();
}
