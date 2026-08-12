package com.wikiknowledge.repository;

import com.wikiknowledge.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 会话仓储 */
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
