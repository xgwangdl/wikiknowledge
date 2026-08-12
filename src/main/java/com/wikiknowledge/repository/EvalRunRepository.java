package com.wikiknowledge.repository;

import com.wikiknowledge.domain.EvalRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 评估运行仓储 */
public interface EvalRunRepository extends JpaRepository<EvalRun, Long> {

    List<EvalRun> findByEvalSetIdOrderByCreatedAtDesc(Long evalSetId);

    List<EvalRun> findAllByOrderByCreatedAtDesc();
}
