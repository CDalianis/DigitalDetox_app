package com.digitaldetox.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "coach_profiles")
public class CoachProfile extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    private String specialty;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private boolean approved;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @OneToMany(mappedBy = "coachProfile", fetch = FetchType.LAZY)
    private Set<DetoxPlan> detoxPlans = new HashSet<>();

    @OneToMany(mappedBy = "coachProfile", fetch = FetchType.LAZY)
    private Set<WeeklyReview> weeklyReviews = new HashSet<>();

    @PrePersist
    public void initializeUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public void addUser(User user) {
        this.user = user;
        user.setCoachProfile(this);
    }

    public void addDetoxPlan(DetoxPlan detoxPlan) {
        detoxPlans.add(detoxPlan);
        detoxPlan.setCoachProfile(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CoachProfile that)) return false;
        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }
}
