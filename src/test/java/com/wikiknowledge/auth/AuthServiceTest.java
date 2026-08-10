package com.wikiknowledge.auth;

import com.wikiknowledge.auth.dto.AuthResponse;
import com.wikiknowledge.auth.dto.LoginRequest;
import com.wikiknowledge.auth.dto.RefreshRequest;
import com.wikiknowledge.auth.dto.RegisterRequest;
import com.wikiknowledge.auth.dto.UserResponse;
import com.wikiknowledge.domain.User;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @InjectMocks
    private AuthService authService;

    @Test
    void registersNewUser() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = authService.register(
                new RegisterRequest("alice", "password123", "Alice"));

        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void rejectsDuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("alice", "password123", "Alice")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");
    }

    @Test
    void loginReturnsTokens() {
        User user = activeUser("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.refreshTtl()).thenReturn(Duration.ofDays(7));

        AuthResponse response = authService.login(new LoginRequest("alice", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenStore).save("alice", "refresh-token", Duration.ofDays(7));
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = activeUser("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "wrong-password")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名或密码错误");
    }

    @Test
    void refreshRotatesToken() {
        User user = activeUser("alice");
        Claims claims = mock(Claims.class);
        when(jwtService.parseToken("old-refresh-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("alice");
        when(refreshTokenStore.matches("alice", "old-refresh-token")).thenReturn(true);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(jwtService.refreshTtl()).thenReturn(Duration.ofDays(7));

        AuthResponse response = authService.refresh(new RefreshRequest("old-refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenStore).delete("alice");
        verify(refreshTokenStore).save("alice", "new-refresh-token", Duration.ofDays(7));
    }

    @Test
    void refreshRejectsUnknownToken() {
        Claims claims = mock(Claims.class);
        when(jwtService.parseToken("unknown-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("alice");
        when(refreshTokenStore.matches("alice", "unknown-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("unknown-token")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("刷新令牌无效或已过期");
    }

    private User activeUser(String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setRole("ROLE_USER");
        user.setStatus("ACTIVE");
        return user;
    }
}
