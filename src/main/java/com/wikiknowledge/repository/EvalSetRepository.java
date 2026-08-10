package com.wikiknowledge.repository;

import com.wikiknowledge.domain.EvalSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvalSetRepository extends JpaRepository<EvalSet, Long> {

    List<EvalSet> findAllByOrderByCreatedAtDesc();
}
