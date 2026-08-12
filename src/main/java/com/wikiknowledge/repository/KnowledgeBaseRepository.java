package com.wikiknowledge.repository;

import com.wikiknowledge.domain.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 知识库仓储 */
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    List<KnowledgeBase> findAllByOrderByCreatedAtDesc();
}
