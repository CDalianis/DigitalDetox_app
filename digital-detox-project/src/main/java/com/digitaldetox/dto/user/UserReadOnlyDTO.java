package com.digitaldetox.dto.user;

public record UserReadOnlyDTO(
        String uuid,
        String username,
        String email,
        String role
) {
}
