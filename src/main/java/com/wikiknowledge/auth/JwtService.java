package com.wikiknowledge.auth;

import com.wikiknowledge.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-token-ttl-minutes:30}") long accessTtlMinutes,
                      @Value("${app.jwt.refresh-token-ttl-days:7}") long refreshTtlDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtl = Duration.ofMinutes(accessTtlMinutes);
        this.refreshTtl = Duration.ofDays(refreshTtlDays);
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessTtl);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTtl);
    }

    public Duration refreshTtl() {
        return refreshTtl;
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtException("invalid token");
        }
    }

    private String generateToken(User user, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole())
                .claim("uid", user.getId())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }
}
