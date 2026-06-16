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
@Table(name = "member_profiles")
public class MemberProfile extends AbstractEntity {

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

    private String timezone;

    @Column(name = "main_goal", length = 500)
    private String mainGoal;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "memberProfile", fetch = FetchType.LAZY)
    private Set<DetoxPlan> detoxPlans = new HashSet<>();

    @PrePersist
    public void initializeUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public void addUser(User user) {
        this.user = user;
        user.setMemberProfile(this);
    }

    public void addDetoxPlan(DetoxPlan detoxPlan) {
        detoxPlans.add(detoxPlan);
        detoxPlan.setMemberProfile(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MemberProfile that)) return false;
        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }
}
