package com.digitaldetox.service;

import com.digitaldetox.core.exceptions.EntityInvalidArgumentException;
import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.dto.plan.DetoxPlanInsertDTO;
import com.digitaldetox.dto.plan.DetoxPlanReadOnlyDTO;
import com.digitaldetox.mapper.Mapper;
import com.digitaldetox.model.CoachProfile;
import com.digitaldetox.model.DetoxPlan;
import com.digitaldetox.model.MemberProfile;
import com.digitaldetox.model.enums.DetoxPlanStatus;
import com.digitaldetox.repository.CoachProfileRepository;
import com.digitaldetox.repository.DetoxPlanRepository;
import com.digitaldetox.repository.MemberProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class DetoxPlanServiceImplTest {

    @Mock
    private DetoxPlanRepository detoxPlanRepository;
    @Mock
    private MemberProfileRepository memberProfileRepository;
    @Mock
    private CoachProfileRepository coachProfileRepository;

    private DetoxPlanServiceImpl service;

    private UUID memberUuid;
    private DetoxPlanInsertDTO insertDto;

    @BeforeEach
    void setUp() {
        service = new DetoxPlanServiceImpl(
                detoxPlanRepository,
                memberProfileRepository,
                coachProfileRepository,
                new Mapper());
        memberUuid = UUID.randomUUID();
        insertDto = new DetoxPlanInsertDTO(
                memberUuid,
                "Weekend unwind",
                "Cut evening screen time",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                DetoxPlanStatus.ACTIVE,
                120,
                45,
                "Social media");
    }

    @Test
    void createPlanPersistsPlanForApprovedCoach() throws Exception {
        CoachProfile coach = approvedCoach();
        MemberProfile member = member(memberUuid);

        when(coachProfileRepository.findByUserUsernameAndDeletedFalse("coach1")).thenReturn(Optional.of(coach));
        when(memberProfileRepository.findByUuidAndDeletedFalse(memberUuid)).thenReturn(Optional.of(member));
        when(detoxPlanRepository.save(any(DetoxPlan.class))).thenAnswer(invocation -> {
            DetoxPlan plan = invocation.getArgument(0);
            plan.setUuid(UUID.randomUUID());
            return plan;
        });

        DetoxPlanReadOnlyDTO result = service.createPlan("coach1", insertDto);

        assertEquals("Weekend unwind", result.title());
        assertEquals(DetoxPlanStatus.ACTIVE, result.status());
        assertEquals(memberUuid.toString(), result.memberProfileUuid());
        assertEquals(coach.getUuid().toString(), result.coachProfileUuid());
        verify(detoxPlanRepository).save(any(DetoxPlan.class));
    }

    @Test
    void createPlanRejectsUnapprovedCoach() {
        CoachProfile coach = approvedCoach();
        coach.setApproved(false);

        when(coachProfileRepository.findByUserUsernameAndDeletedFalse("coach1")).thenReturn(Optional.of(coach));

        assertThrows(EntityInvalidArgumentException.class, () -> service.createPlan("coach1", insertDto));
        verify(detoxPlanRepository, never()).save(any(DetoxPlan.class));
    }

    @Test
    void createPlanThrowsWhenMemberMissing() {
        when(coachProfileRepository.findByUserUsernameAndDeletedFalse("coach1"))
                .thenReturn(Optional.of(approvedCoach()));
        when(memberProfileRepository.findByUuidAndDeletedFalse(memberUuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.createPlan("coach1", insertDto));
        verify(detoxPlanRepository, never()).save(any(DetoxPlan.class));
    }

    @Test
    void getByUuidThrowsWhenPlanMissing() {
        UUID planUuid = UUID.randomUUID();
        when(detoxPlanRepository.findByUuidAndDeletedFalse(planUuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getByUuid(planUuid));
    }

    @Test
    void updateStatusChangesPlanStatus() throws Exception {
        UUID planUuid = UUID.randomUUID();
        DetoxPlan plan = existingPlan(planUuid, memberUuid);

        when(detoxPlanRepository.findByUuidAndDeletedFalse(planUuid)).thenReturn(Optional.of(plan));

        DetoxPlanReadOnlyDTO result = service.updateStatus(planUuid, DetoxPlanStatus.COMPLETED);

        assertEquals(DetoxPlanStatus.COMPLETED, result.status());
        assertEquals(DetoxPlanStatus.COMPLETED, plan.getStatus());
    }

    private static CoachProfile approvedCoach() {
        CoachProfile coach = new CoachProfile();
        coach.setUuid(UUID.randomUUID());
        coach.setDisplayName("Alex Coach");
        coach.setApproved(true);
        return coach;
    }

    private static MemberProfile member(UUID uuid) {
        MemberProfile member = new MemberProfile();
        member.setUuid(uuid);
        member.setDisplayName("Sam Member");
        return member;
    }

    private static DetoxPlan existingPlan(UUID planUuid, UUID memberUuid) {
        DetoxPlan plan = new DetoxPlan();
        plan.setUuid(planUuid);
        plan.setTitle("Weekend unwind");
        plan.setStartDate(LocalDate.of(2026, 9, 1));
        plan.setStatus(DetoxPlanStatus.ACTIVE);
        plan.setMemberProfile(member(memberUuid));
        plan.setCoachProfile(approvedCoach());
        return plan;
    }
}
