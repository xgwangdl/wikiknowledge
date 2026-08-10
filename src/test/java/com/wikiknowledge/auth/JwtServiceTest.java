package com.wikiknowledge.auth;

import com.wikiknowledge.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "wikiknowledge-test-secret-key-2026-0123456789abcdef",
            30,
            7
    );

    @Test
    void generatesAndParsesAccessToken() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole("ROLE_USER");

        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("alice");
        assertThat(claims.get("role")).isEqualTo("ROLE_USER");
        assertThat(claims.get("uid", Long.class)).isEqualTo(1L);
    }

    @Test
    void rejectsInvalidToken() {
        assertThatThrownBy(() -> jwtService.parseToken("invalid.token.value"))
                .isInstanceOf(JwtException.class);
    }
}
