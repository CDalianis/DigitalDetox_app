package com.digitaldetox.repository;

import com.digitaldetox.model.CoachProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoachProfileRepository extends JpaRepository<CoachProfile, Long>,
        JpaSpecificationExecutor<CoachProfile> {

    Optional<CoachProfile> findByUuid(UUID uuid);

    Optional<CoachProfile> findByUuidAndDeletedFalse(UUID uuid);

    @EntityGraph(attributePaths = {"user", "user.role"})
    Optional<CoachProfile> findByUserUsernameAndDeletedFalse(String username);

    List<CoachProfile> findAllByApprovedTrueAndDeletedFalseOrderByDisplayNameAsc();

    List<CoachProfile> findAllByApprovedFalseAndDeletedFalseOrderByDisplayNameAsc();

    boolean existsByUserIdAndDeletedFalse(Long userId);
}
