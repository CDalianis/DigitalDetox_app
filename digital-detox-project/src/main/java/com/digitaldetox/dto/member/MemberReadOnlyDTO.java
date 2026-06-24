package com.digitaldetox.dto.member;

public record MemberReadOnlyDTO(
        String uuid,
        String displayName,
        String timezone,
        String mainGoal,
        String username,
        String email
) {
}
