package com.digitaldetox.dto.review;

import com.digitaldetox.model.enums.RiskLevel;

import java.time.LocalDate;

public record WeeklyReviewReadOnlyDTO(
        String uuid,
        String detoxPlanUuid,
        String coachProfileUuid,
        String coachDisplayName,
        LocalDate weekStart,
        String summary,
        String recommendation,
        RiskLevel riskLevel
) {
}
