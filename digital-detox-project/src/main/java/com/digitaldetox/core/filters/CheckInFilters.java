package com.digitaldetox.core.filters;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInFilters {

    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer minScreenMinutes;
    private Integer maxScreenMinutes;
}
