package com.digitaldetox.service;

import com.digitaldetox.core.exceptions.EntityAlreadyExistsException;
import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.dto.checkin.CheckInInsertDTO;
import com.digitaldetox.dto.checkin.CheckInReadOnlyDTO;
import com.digitaldetox.mapper.Mapper;
import com.digitaldetox.model.DailyCheckIn;
import com.digitaldetox.model.DetoxPlan;
import com.digitaldetox.repository.DailyCheckInRepository;
import com.digitaldetox.repository.DetoxPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckInServiceImplTest {

    @Mock
    private DailyCheckInRepository dailyCheckInRepository;
    @Mock
    private DetoxPlanRepository detoxPlanRepository;

    private CheckInServiceImpl service;

    private UUID planUuid;
    private CheckInInsertDTO insertDto;

    @BeforeEach
    void setUp() {
        service = new CheckInServiceImpl(dailyCheckInRepository, detoxPlanRepository, new Mapper());
        planUuid = UUID.randomUUID();
        insertDto = new CheckInInsertDTO(
                LocalDate.of(2026, 9, 3),
                95,
                30,
                new BigDecimal("7.5"),
                8,
                4,
                3,
                "Logged off after dinner");
    }

    @Test
    void createCheckInPersistsEntryForExistingPlan() throws Exception {
        DetoxPlan plan = plan(planUuid);
        when(detoxPlanRepository.findByUuidAndDeletedFalse(planUuid)).thenReturn(Optional.of(plan));
        when(dailyCheckInRepository.existsByDetoxPlanUuidAndEntryDateAndDeletedFalse(planUuid, insertDto.entryDate()))
                .thenReturn(false);
        when(dailyCheckInRepository.save(any(DailyCheckIn.class))).thenAnswer(invocation -> {
            DailyCheckIn checkIn = invocation.getArgument(0);
            checkIn.setUuid(UUID.randomUUID());
            return checkIn;
        });

        CheckInReadOnlyDTO result = service.createCheckIn(planUuid, insertDto);

        assertEquals(planUuid.toString(), result.detoxPlanUuid());
        assertEquals(insertDto.entryDate(), result.entryDate());
        assertEquals(95, result.totalScreenMinutes());
        assertEquals(1, plan.getDailyCheckIns().size());
        verify(dailyCheckInRepository).save(any(DailyCheckIn.class));
    }

    @Test
    void createCheckInRejectsDuplicateDate() {
        when(detoxPlanRepository.findByUuidAndDeletedFalse(planUuid)).thenReturn(Optional.of(plan(planUuid)));
        when(dailyCheckInRepository.existsByDetoxPlanUuidAndEntryDateAndDeletedFalse(planUuid, insertDto.entryDate()))
                .thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () -> service.createCheckIn(planUuid, insertDto));
        verify(dailyCheckInRepository, never()).save(any(DailyCheckIn.class));
    }

    @Test
    void createCheckInThrowsWhenPlanMissing() {
        when(detoxPlanRepository.findByUuidAndDeletedFalse(planUuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.createCheckIn(planUuid, insertDto));
        verify(dailyCheckInRepository, never()).save(any(DailyCheckIn.class));
    }

    private static DetoxPlan plan(UUID uuid) {
        DetoxPlan plan = new DetoxPlan();
        plan.setUuid(uuid);
        plan.setTitle("Weekend unwind");
        return plan;
    }
}
