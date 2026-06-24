package com.digitaldetox.dto.goal;

import com.digitaldetox.model.enums.GoalStatus;
import com.digitaldetox.model.enums.MetricType;

public record GoalReadOnlyDTO(
        String uuid,
        String title,
        String description,
        MetricType metricType,
        Integer targetValue,
        Integer currentValue,
        GoalStatus status,
        String detoxPlanUuid
) {
}
