package com.wikiknowledge.repository;

import com.wikiknowledge.domain.EvalQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvalQuestionRepository extends JpaRepository<EvalQuestion, Long> {

    List<EvalQuestion> findByEvalSetIdOrderByIdAsc(Long evalSetId);
}
