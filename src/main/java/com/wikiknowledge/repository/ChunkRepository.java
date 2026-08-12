package com.wikiknowledge.repository;

import com.wikiknowledge.domain.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** 切片仓储，包含向量更新与相似检索 */
public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    void deleteByDocumentId(Long documentId);

    List<Chunk> findTop3ByKnowledgeBaseIdOrderByIdAsc(Long knowledgeBaseId);

    @Modifying
    @Query(value = "UPDATE chunks SET embedding = CAST(:embedding AS vector) WHERE id = :id", nativeQuery = true)
    void updateEmbedding(@Param("id") Long id, @Param("embedding") String embedding);

    @Query(value = """
            SELECT id,
                   document_id AS documentId,
                   knowledge_base_id AS knowledgeBaseId,
                   content,
                   seq_no AS seqNo,
                   1 - (embedding <=> CAST(:embedding AS vector)) AS similarity
            FROM chunks
            WHERE knowledge_base_id = :knowledgeBaseId AND embedding IS NOT NULL
            ORDER BY embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<ChunkMatch> searchSimilar(@Param("knowledgeBaseId") Long knowledgeBaseId,
                                   @Param("embedding") String embedding,
                                   @Param("limit") int limit);
}
