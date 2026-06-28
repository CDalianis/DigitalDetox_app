package com.digitaldetox.specification;

import com.digitaldetox.core.filters.CheckInFilters;
import com.digitaldetox.model.DailyCheckIn;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class CheckInSpecification {

    private CheckInSpecification() {
    }

    public static Specification<DailyCheckIn> build(UUID planUuid, CheckInFilters filters) {
        return Specification.allOf(
                belongsToPlan(planUuid),
                fromDate(filters.getFromDate()),
                toDate(filters.getToDate()),
                minScreenMinutes(filters.getMinScreenMinutes()),
                maxScreenMinutes(filters.getMaxScreenMinutes()),
                isNotDeleted()
        );
    }

    private static Specification<DailyCheckIn> belongsToPlan(UUID planUuid) {
        return (root, query, cb) -> cb.equal(root.get("detoxPlan").get("uuid"), planUuid);
    }

    private static Specification<DailyCheckIn> fromDate(java.time.LocalDate fromDate) {
        return (root, query, cb) -> fromDate == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("entryDate"), fromDate);
    }

    private static Specification<DailyCheckIn> toDate(java.time.LocalDate toDate) {
        return (root, query, cb) -> toDate == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("entryDate"), toDate);
    }

    private static Specification<DailyCheckIn> minScreenMinutes(Integer min) {
        return (root, query, cb) -> min == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("totalScreenMinutes"), min);
    }

    private static Specification<DailyCheckIn> maxScreenMinutes(Integer max) {
        return (root, query, cb) -> max == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("totalScreenMinutes"), max);
    }

    private static Specification<DailyCheckIn> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }
}
