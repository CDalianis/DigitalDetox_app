package com.digitaldetox.repository;

import com.digitaldetox.model.MemberProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long>,
        JpaSpecificationExecutor<MemberProfile> {

    Optional<MemberProfile> findByUuid(UUID uuid);

    Optional<MemberProfile> findByUuidAndDeletedFalse(UUID uuid);

    @EntityGraph(attributePaths = {"user", "user.role"})
    Optional<MemberProfile> findByUserUsernameAndDeletedFalse(String username);

    boolean existsByUserIdAndDeletedFalse(Long userId);

    java.util.List<MemberProfile> findAllByDeletedFalseOrderByDisplayNameAsc();
}
