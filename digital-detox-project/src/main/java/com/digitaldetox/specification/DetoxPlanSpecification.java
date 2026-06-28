package com.digitaldetox.specification;

import com.digitaldetox.core.filters.DetoxPlanFilters;
import com.digitaldetox.model.DetoxPlan;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class DetoxPlanSpecification {

    private DetoxPlanSpecification() {
    }

    public static Specification<DetoxPlan> build(DetoxPlanFilters filters) {
        return Specification.allOf(
                hasMemberUuid(filters.getMemberProfileUuid()),
                hasCoachUuid(filters.getCoachProfileUuid()),
                hasStatus(filters.getStatus()),
                titleContains(filters.getTitle()),
                focusAreaContains(filters.getFocusArea()),
                isNotDeleted()
        );
    }

    private static Specification<DetoxPlan> hasMemberUuid(UUID memberUuid) {
        return (root, query, cb) -> memberUuid == null ? cb.conjunction()
                : cb.equal(root.get("memberProfile").get("uuid"), memberUuid);
    }

    private static Specification<DetoxPlan> hasCoachUuid(UUID coachUuid) {
        return (root, query, cb) -> coachUuid == null ? cb.conjunction()
                : cb.equal(root.get("coachProfile").get("uuid"), coachUuid);
    }

    private static Specification<DetoxPlan> hasStatus(com.digitaldetox.model.enums.DetoxPlanStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    private static Specification<DetoxPlan> titleContains(String title) {
        return (root, query, cb) -> title == null || title.isBlank() ? cb.conjunction()
                : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private static Specification<DetoxPlan> focusAreaContains(String focusArea) {
        return (root, query, cb) -> focusArea == null || focusArea.isBlank() ? cb.conjunction()
                : cb.like(cb.lower(root.get("focusArea")), "%" + focusArea.toLowerCase() + "%");
    }

    private static Specification<DetoxPlan> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }
}
