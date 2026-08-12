package com.wikiknowledge.repository;

import com.wikiknowledge.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 用户仓储 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
