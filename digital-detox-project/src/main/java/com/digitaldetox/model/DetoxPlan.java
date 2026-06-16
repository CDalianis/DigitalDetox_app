package com.digitaldetox.model;

import com.digitaldetox.model.enums.DetoxPlanStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "detox_plans")
public class DetoxPlan extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_profile_id", nullable = false)
    private MemberProfile memberProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_profile_id", nullable = false)
    private CoachProfile coachProfile;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DetoxPlanStatus status;

    @Column(name = "target_screen_minutes")
    private Integer targetScreenMinutes;

    @Column(name = "target_social_minutes")
    private Integer targetSocialMinutes;

    @Column(name = "focus_area")
    private String focusArea;

    @OneToMany(mappedBy = "detoxPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Goal> goals = new HashSet<>();

    @OneToMany(mappedBy = "detoxPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DailyCheckIn> dailyCheckIns = new HashSet<>();

    @OneToMany(mappedBy = "detoxPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WeeklyReview> weeklyReviews = new HashSet<>();

    @PrePersist
    public void initializeUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public void addGoal(Goal goal) {
        goals.add(goal);
        goal.setDetoxPlan(this);
    }

    public void addDailyCheckIn(DailyCheckIn checkIn) {
        dailyCheckIns.add(checkIn);
        checkIn.setDetoxPlan(this);
    }

    public void addWeeklyReview(WeeklyReview review) {
        weeklyReviews.add(review);
        review.setDetoxPlan(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DetoxPlan that)) return false;
        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }
}
