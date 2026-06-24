package com.digitaldetox.dto.checkin;

import com.digitaldetox.dto.attachment.AttachmentReadOnlyDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CheckInReadOnlyDTO(
        String uuid,
        String detoxPlanUuid,
        LocalDate entryDate,
        Integer totalScreenMinutes,
        Integer socialMediaMinutes,
        BigDecimal sleepHours,
        Integer focusScore,
        Integer stressLevel,
        Integer cravingLevel,
        String notes,
        List<AttachmentReadOnlyDTO> attachments
) {
}
