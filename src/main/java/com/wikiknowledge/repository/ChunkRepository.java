package com.wikiknowledge.repository;

import com.wikiknowledge.domain.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    void deleteByDocumentId(Long documentId);
}
