package com.digitaldetox.dto.goal;

import com.digitaldetox.model.enums.GoalStatus;
import com.digitaldetox.model.enums.MetricType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GoalInsertDTO(
        @NotBlank String title,
        String description,
        @NotNull MetricType metricType,
        @NotNull Integer targetValue,
        @NotNull GoalStatus status
) {
}
