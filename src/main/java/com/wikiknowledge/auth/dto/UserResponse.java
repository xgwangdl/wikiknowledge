package com.wikiknowledge.auth.dto;

import com.wikiknowledge.domain.User;

public record UserResponse(Long id, String username, String displayName, String role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
    }
}
