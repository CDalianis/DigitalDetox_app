package com.digitaldetox.dto.auth;

public record AuthenticationResponseDTO(
        String token,
        String role,
        String displayName
) {
}
