package com.wikiknowledge.repository;

import com.wikiknowledge.domain.EvalResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvalResultRepository extends JpaRepository<EvalResult, Long> {

    List<EvalResult> findByEvalRunIdOrderByIdAsc(Long evalRunId);
}
