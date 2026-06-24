package com.digitaldetox.dto.coach;

import com.digitaldetox.dto.user.UserInsertDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CoachRegisterDTO(
        @NotNull @Valid UserInsertDTO user,
        @NotBlank String displayName,
        String specialty,
        String bio,
        Integer yearsExperience
) {
}
