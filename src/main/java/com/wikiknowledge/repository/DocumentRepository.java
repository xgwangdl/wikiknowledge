package com.wikiknowledge.repository;

import com.wikiknowledge.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByKnowledgeBaseIdOrderByCreatedAtDesc(Long knowledgeBaseId);

    boolean existsByKnowledgeBaseIdAndFileHash(Long knowledgeBaseId, String fileHash);
}
