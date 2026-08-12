package com.wikiknowledge.repository;

import com.wikiknowledge.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 会话消息仓储 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySessionIdOrderByIdAsc(Long sessionId);
}
