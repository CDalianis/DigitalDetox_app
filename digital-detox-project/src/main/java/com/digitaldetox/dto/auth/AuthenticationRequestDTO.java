package com.digitaldetox.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequestDTO(
        @NotBlank String username,
        @NotBlank String password
) {
}
