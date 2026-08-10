package com.wikiknowledge.repository;

import com.wikiknowledge.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySessionIdOrderByIdAsc(Long sessionId);
}
