package com.digitaldetox.dto.member;

import jakarta.validation.constraints.NotBlank;

public record MemberUpdateDTO(
        @NotBlank String displayName,
        String timezone,
        String mainGoal,
        String notes
) {
}
