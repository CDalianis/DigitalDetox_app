package com.digitaldetox.core.filters;

import com.digitaldetox.model.enums.DetoxPlanStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetoxPlanFilters {

    private UUID memberProfileUuid;
    private UUID coachProfileUuid;
    private DetoxPlanStatus status;
    private String title;
    private String focusArea;
}
