package com.digitaldetox.service;

import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.dto.coach.CoachReadOnlyDTO;
import com.digitaldetox.mapper.Mapper;
import com.digitaldetox.model.CoachProfile;
import com.digitaldetox.model.User;
import com.digitaldetox.repository.CoachProfileRepository;
import com.digitaldetox.repository.RoleRepository;
import com.digitaldetox.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoachServiceImplTest {

    @Mock
    private CoachProfileRepository coachProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private CoachServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CoachServiceImpl(
                coachProfileRepository,
                userRepository,
                roleRepository,
                new Mapper(),
                passwordEncoder);
    }

    @Test
    void approveCoachMarksProfileApproved() throws Exception {
        UUID coachUuid = UUID.randomUUID();
        CoachProfile profile = pendingCoach(coachUuid);
        when(coachProfileRepository.findByUuidAndDeletedFalse(coachUuid)).thenReturn(Optional.of(profile));

        assertFalse(profile.isApproved());

        CoachReadOnlyDTO result = service.approveCoach(coachUuid);

        assertTrue(profile.isApproved());
        assertTrue(result.approved());
        verify(coachProfileRepository).findByUuidAndDeletedFalse(coachUuid);
    }

    @Test
    void approveCoachThrowsWhenCoachMissing() {
        UUID coachUuid = UUID.randomUUID();
        when(coachProfileRepository.findByUuidAndDeletedFalse(coachUuid)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.approveCoach(coachUuid));
    }

    private static CoachProfile pendingCoach(UUID uuid) {
        User user = new User("pending.coach", "coach@example.com", "encoded");
        CoachProfile profile = new CoachProfile();
        profile.setUuid(uuid);
        profile.setDisplayName("Pending Coach");
        profile.setApproved(false);
        profile.addUser(user);
        return profile;
    }
}
