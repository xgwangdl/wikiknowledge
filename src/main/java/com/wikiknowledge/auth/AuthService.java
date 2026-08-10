package com.wikiknowledge.auth;

import com.wikiknowledge.auth.dto.AuthResponse;
import com.wikiknowledge.auth.dto.LoginRequest;
import com.wikiknowledge.auth.dto.RegisterRequest;
import com.wikiknowledge.auth.dto.RefreshRequest;
import com.wikiknowledge.auth.dto.UserResponse;
import com.wikiknowledge.domain.User;
import com.wikiknowledge.exception.BusinessException;
import com.wikiknowledge.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenStore refreshTokenStore) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = findActiveUser(request.username());
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("BAD_CREDENTIALS", "用户名或密码错误");
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        Claims claims = jwtService.parseToken(request.refreshToken());
        String username = claims.getSubject();
        if (!refreshTokenStore.matches(username, request.refreshToken())) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "刷新令牌无效或已过期");
        }
        User user = findActiveUser(username);
        refreshTokenStore.delete(username);
        return issueTokens(user);
    }

    @Transactional
    public void logout(String username) {
        refreshTokenStore.delete(username);
    }

    @Transactional(readOnly = true)
    public UserResponse me(String username) {
        return UserResponse.from(findActiveUser(username));
    }

    private User findActiveUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException("USER_DISABLED", "用户已被禁用");
        }
        return user;
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenStore.save(user.getUsername(), refreshToken, jwtService.refreshTtl());
        return new AuthResponse(accessToken, refreshToken, UserResponse.from(user));
    }
}
