package com.wikiknowledge.repository;

/** 混合检索融合后的切片结果实现。 */
public class ChunkMatchResult implements ChunkMatch {

    private final Long id;
    private final Long documentId;
    private final Long knowledgeBaseId;
    private final String content;
    private final Integer seqNo;
    private final Double similarity;

    public ChunkMatchResult(Long id,
                            Long documentId,
                            Long knowledgeBaseId,
                            String content,
                            Integer seqNo,
                            Double similarity) {
        this.id = id;
        this.documentId = documentId;
        this.knowledgeBaseId = knowledgeBaseId;
        this.content = content;
        this.seqNo = seqNo;
        this.similarity = similarity;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getDocumentId() {
        return documentId;
    }

    @Override
    public Long getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Integer getSeqNo() {
        return seqNo;
    }

    @Override
    public Double getSimilarity() {
        return similarity;
    }
}
