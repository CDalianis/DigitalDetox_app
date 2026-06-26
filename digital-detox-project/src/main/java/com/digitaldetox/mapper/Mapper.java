package com.digitaldetox.mapper;

import com.digitaldetox.dto.attachment.AttachmentReadOnlyDTO;
import com.digitaldetox.dto.checkin.CheckInInsertDTO;
import com.digitaldetox.dto.checkin.CheckInReadOnlyDTO;
import com.digitaldetox.dto.coach.CoachReadOnlyDTO;
import com.digitaldetox.dto.coach.CoachRegisterDTO;
import com.digitaldetox.dto.goal.GoalInsertDTO;
import com.digitaldetox.dto.goal.GoalReadOnlyDTO;
import com.digitaldetox.dto.member.MemberReadOnlyDTO;
import com.digitaldetox.dto.member.MemberRegisterDTO;
import com.digitaldetox.dto.plan.DetoxPlanInsertDTO;
import com.digitaldetox.dto.plan.DetoxPlanReadOnlyDTO;
import com.digitaldetox.dto.plan.DetoxPlanUpdateDTO;
import com.digitaldetox.dto.review.WeeklyReviewInsertDTO;
import com.digitaldetox.dto.review.WeeklyReviewReadOnlyDTO;
import com.digitaldetox.dto.user.UserReadOnlyDTO;
import com.digitaldetox.model.*;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {
        return new UserReadOnlyDTO(
                user.getUuid().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName()
        );
    }

    public MemberProfile mapToMemberProfile(MemberRegisterDTO dto) {
        MemberProfile profile = new MemberProfile();
        profile.setDisplayName(dto.displayName());
        profile.setTimezone(dto.timezone());
        profile.setMainGoal(dto.mainGoal());

        User user = new User(dto.user().username(), dto.user().email(), dto.user().password());
        profile.addUser(user);
        return profile;
    }

    public MemberReadOnlyDTO mapToMemberReadOnlyDTO(MemberProfile profile) {
        return new MemberReadOnlyDTO(
                profile.getUuid().toString(),
                profile.getDisplayName(),
                profile.getTimezone(),
                profile.getMainGoal(),
                profile.getUser().getUsername(),
                profile.getUser().getEmail()
        );
    }

    public CoachProfile mapToCoachProfile(CoachRegisterDTO dto) {
        CoachProfile profile = new CoachProfile();
        profile.setDisplayName(dto.displayName());
        profile.setSpecialty(dto.specialty());
        profile.setBio(dto.bio());
        profile.setYearsExperience(dto.yearsExperience());
        profile.setApproved(false);

        User user = new User(dto.user().username(), dto.user().email(), dto.user().password());
        profile.addUser(user);
        return profile;
    }

    public CoachReadOnlyDTO mapToCoachReadOnlyDTO(CoachProfile profile) {
        return new CoachReadOnlyDTO(
                profile.getUuid().toString(),
                profile.getDisplayName(),
                profile.getSpecialty(),
                profile.getBio(),
                profile.isApproved(),
                profile.getYearsExperience(),
                profile.getUser().getUsername(),
                profile.getUser().getEmail()
        );
    }

    public DetoxPlan mapToDetoxPlanEntity(DetoxPlanInsertDTO dto) {
        DetoxPlan plan = new DetoxPlan();
        plan.setTitle(dto.title());
        plan.setDescription(dto.description());
        plan.setStartDate(dto.startDate());
        plan.setEndDate(dto.endDate());
        plan.setStatus(dto.status());
        plan.setTargetScreenMinutes(dto.targetScreenMinutes());
        plan.setTargetSocialMinutes(dto.targetSocialMinutes());
        plan.setFocusArea(dto.focusArea());
        return plan;
    }

    public void updateDetoxPlan(DetoxPlan plan, DetoxPlanUpdateDTO dto) {
        plan.setTitle(dto.title());
        plan.setDescription(dto.description());
        plan.setStartDate(dto.startDate());
        plan.setEndDate(dto.endDate());
        plan.setStatus(dto.status());
        plan.setTargetScreenMinutes(dto.targetScreenMinutes());
        plan.setTargetSocialMinutes(dto.targetSocialMinutes());
        plan.setFocusArea(dto.focusArea());
    }

    public DetoxPlanReadOnlyDTO mapToDetoxPlanReadOnlyDTO(DetoxPlan plan) {
        return new DetoxPlanReadOnlyDTO(
                plan.getUuid().toString(),
                plan.getTitle(),
                plan.getDescription(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getStatus(),
                plan.getTargetScreenMinutes(),
                plan.getTargetSocialMinutes(),
                plan.getFocusArea(),
                plan.getMemberProfile().getUuid().toString(),
                plan.getMemberProfile().getDisplayName(),
                plan.getCoachProfile().getUuid().toString(),
                plan.getCoachProfile().getDisplayName()
        );
    }

    public Goal mapToGoalEntity(GoalInsertDTO dto) {
        Goal goal = new Goal();
        goal.setTitle(dto.title());
        goal.setDescription(dto.description());
        goal.setMetricType(dto.metricType());
        goal.setTargetValue(dto.targetValue());
        goal.setCurrentValue(0);
        goal.setStatus(dto.status());
        return goal;
    }

    public GoalReadOnlyDTO mapToGoalReadOnlyDTO(Goal goal) {
        return new GoalReadOnlyDTO(
                goal.getUuid().toString(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getMetricType(),
                goal.getTargetValue(),
                goal.getCurrentValue(),
                goal.getStatus(),
                goal.getDetoxPlan().getUuid().toString()
        );
    }

    public DailyCheckIn mapToCheckInEntity(CheckInInsertDTO dto) {
        DailyCheckIn checkIn = new DailyCheckIn();
        checkIn.setEntryDate(dto.entryDate());
        checkIn.setTotalScreenMinutes(dto.totalScreenMinutes());
        checkIn.setSocialMediaMinutes(dto.socialMediaMinutes());
        checkIn.setSleepHours(dto.sleepHours());
        checkIn.setFocusScore(dto.focusScore());
        checkIn.setStressLevel(dto.stressLevel());
        checkIn.setCravingLevel(dto.cravingLevel());
        checkIn.setNotes(dto.notes());
        return checkIn;
    }

    public AttachmentReadOnlyDTO mapToAttachmentReadOnlyDTO(Attachment attachment) {
        return new AttachmentReadOnlyDTO(
                attachment.getUuid().toString(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getFileExtension(),
                attachment.getSizeBytes()
        );
    }

    public CheckInReadOnlyDTO mapToCheckInReadOnlyDTO(DailyCheckIn checkIn) {
        return new CheckInReadOnlyDTO(
                checkIn.getUuid().toString(),
                checkIn.getDetoxPlan().getUuid().toString(),
                checkIn.getEntryDate(),
                checkIn.getTotalScreenMinutes(),
                checkIn.getSocialMediaMinutes(),
                checkIn.getSleepHours(),
                checkIn.getFocusScore(),
                checkIn.getStressLevel(),
                checkIn.getCravingLevel(),
                checkIn.getNotes(),
                checkIn.getAttachments().stream().map(this::mapToAttachmentReadOnlyDTO).toList()
        );
    }

    public WeeklyReview mapToWeeklyReviewEntity(WeeklyReviewInsertDTO dto) {
        WeeklyReview review = new WeeklyReview();
        review.setWeekStart(dto.weekStart());
        review.setSummary(dto.summary());
        review.setRecommendation(dto.recommendation());
        review.setRiskLevel(dto.riskLevel());
        return review;
    }

    public WeeklyReviewReadOnlyDTO mapToWeeklyReviewReadOnlyDTO(WeeklyReview review) {
        return new WeeklyReviewReadOnlyDTO(
                review.getUuid().toString(),
                review.getDetoxPlan().getUuid().toString(),
                review.getCoachProfile().getUuid().toString(),
                review.getCoachProfile().getDisplayName(),
                review.getWeekStart(),
                review.getSummary(),
                review.getRecommendation(),
                review.getRiskLevel()
        );
    }
}
