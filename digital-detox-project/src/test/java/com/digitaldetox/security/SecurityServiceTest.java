package com.digitaldetox.security;

import com.digitaldetox.model.CoachProfile;
import com.digitaldetox.model.DailyCheckIn;
import com.digitaldetox.model.DetoxPlan;
import com.digitaldetox.model.MemberProfile;
import com.digitaldetox.model.User;
import com.digitaldetox.repository.CoachProfileRepository;
import com.digitaldetox.repository.DailyCheckInRepository;
import com.digitaldetox.repository.DetoxPlanRepository;
import com.digitaldetox.repository.MemberProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private DetoxPlanRepository detoxPlanRepository;
    @Mock
    private MemberProfileRepository memberProfileRepository;
    @Mock
    private CoachProfileRepository coachProfileRepository;
    @Mock
    private DailyCheckInRepository dailyCheckInRepository;

    private SecurityService securityService;

    private User memberUser;
    private User coachUser;
    private UUID planUuid;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService(
                detoxPlanRepository,
                memberProfileRepository,
                coachProfileRepository,
                dailyCheckInRepository);

        memberUser = user(1L, "sam.member");
        coachUser = user(2L, "alex.coach");
        planUuid = UUID.randomUUID();
    }

    @Test
    void isOwnPlanMemberWhenAuthenticatedUserOwnsThePlan() {
        when(detoxPlanRepository.findByUuidAndDeletedFalse(planUuid)).thenReturn(Optional.of(plan()));

        assertTrue(securityService.isOwnPlanMember(planUuid, auth(memberUser)));
        assertFalse(securityService.isOwnPlanMember(planUuid, auth(coachUser)));
    }

    @Test
    void isAssignedCoachForPlanWhenCoachIsLinkedToThePlan() {
        when(detoxPlanRepository.findByUuidAndDeletedFalse(planUuid)).thenReturn(Optional.of(plan()));

        assertTrue(securityService.isAssignedCoachForPlan(planUuid, auth(coachUser)));
        assertFalse(securityService.isAssignedCoachForPlan(planUuid, auth(memberUser)));
    }

    @Test
    void isPlanParticipantForMemberOrAssignedCoach() {
        when(detoxPlanRepository.findByUuidAndDeletedFalse(planUuid)).thenReturn(Optional.of(plan()));

        assertTrue(securityService.isPlanParticipant(planUuid, auth(memberUser)));
        assertTrue(securityService.isPlanParticipant(planUuid, auth(coachUser)));
        assertFalse(securityService.isPlanParticipant(planUuid, auth(user(99L, "other"))));
    }

    @Test
    void isApprovedCoachReadsApprovalFlag() {
        CoachProfile profile = new CoachProfile();
        profile.setApproved(true);
        when(coachProfileRepository.findByUserUsernameAndDeletedFalse("alex.coach"))
                .thenReturn(Optional.of(profile));

        assertTrue(securityService.isApprovedCoach(auth(coachUser)));
    }

    @Test
    void isOwnCheckInMemberWhenMemberOwnsThePlan() {
        UUID checkInUuid = UUID.randomUUID();
        DailyCheckIn checkIn = new DailyCheckIn();
        checkIn.setDetoxPlan(plan());
        when(dailyCheckInRepository.findByUuidAndDeletedFalse(checkInUuid)).thenReturn(Optional.of(checkIn));

        assertTrue(securityService.isOwnCheckInMember(checkInUuid, auth(memberUser)));
        assertFalse(securityService.isOwnCheckInMember(checkInUuid, auth(coachUser)));
    }

    private DetoxPlan plan() {
        MemberProfile member = new MemberProfile();
        member.setUser(memberUser);
        CoachProfile coach = new CoachProfile();
        coach.setUser(coachUser);

        DetoxPlan plan = new DetoxPlan();
        plan.setUuid(planUuid);
        plan.setMemberProfile(member);
        plan.setCoachProfile(coach);
        return plan;
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private static Authentication auth(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, List.of());
    }
}
