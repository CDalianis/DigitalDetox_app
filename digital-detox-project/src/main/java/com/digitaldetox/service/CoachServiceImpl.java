package com.digitaldetox.service;

import com.digitaldetox.core.exceptions.EntityAlreadyExistsException;
import com.digitaldetox.core.exceptions.EntityInvalidArgumentException;
import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.dto.coach.CoachReadOnlyDTO;
import com.digitaldetox.dto.coach.CoachRegisterDTO;
import com.digitaldetox.dto.coach.CoachUpdateDTO;
import com.digitaldetox.mapper.Mapper;
import com.digitaldetox.model.CoachProfile;
import com.digitaldetox.model.Role;
import com.digitaldetox.model.User;
import com.digitaldetox.repository.CoachProfileRepository;
import com.digitaldetox.repository.RoleRepository;
import com.digitaldetox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoachServiceImpl implements ICoachService {

    private static final String COACH_ROLE = "COACH";

    private final CoachProfileRepository coachProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = {EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    public CoachReadOnlyDTO register(CoachRegisterDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (userRepository.findByUsernameAndDeletedFalse(dto.user().username()).isPresent()) {
            throw new EntityAlreadyExistsException("Username", "User with username " + dto.user().username() + " already exists");
        }
        if (userRepository.findByEmailAndDeletedFalse(dto.user().email()).isPresent()) {
            throw new EntityAlreadyExistsException("Email", "User with email " + dto.user().email() + " already exists");
        }

        Role role = roleRepository.findByName(COACH_ROLE)
                .orElseThrow(() -> new EntityInvalidArgumentException("Role", "Role " + COACH_ROLE + " not found"));

        CoachProfile profile = mapper.mapToCoachProfile(dto);
        User user = profile.getUser();
        user.setPassword(passwordEncoder.encode(dto.user().password()));
        role.addUser(user);

        coachProfileRepository.save(profile);
        log.info("Coach registered with username={}, pending approval", user.getUsername());
        return mapper.mapToCoachReadOnlyDTO(profile);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_COACHES')")
    @Transactional(readOnly = true)
    public CoachReadOnlyDTO getByUuid(UUID uuid) throws EntityNotFoundException {
        CoachProfile profile = coachProfileRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Coach", "Coach with uuid=" + uuid + " not found"));
        return mapper.mapToCoachReadOnlyDTO(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachReadOnlyDTO> getApprovedCoaches() {
        return coachProfileRepository.findAllByApprovedTrueAndDeletedFalseOrderByDisplayNameAsc()
                .stream()
                .map(mapper::mapToCoachReadOnlyDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasAuthority('APPROVE_COACH')")
    @Transactional(readOnly = true)
    public List<CoachReadOnlyDTO> getPendingCoaches() {
        return coachProfileRepository.findAllByApprovedFalseAndDeletedFalseOrderByDisplayNameAsc()
                .stream()
                .map(mapper::mapToCoachReadOnlyDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasAuthority('APPROVE_COACH')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public CoachReadOnlyDTO approveCoach(UUID uuid) throws EntityNotFoundException {
        CoachProfile profile = coachProfileRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Coach", "Coach with uuid=" + uuid + " not found"));
        profile.setApproved(true);
        log.info("Coach approved uuid={}", uuid);
        return mapper.mapToCoachReadOnlyDTO(profile);
    }

    @Override
    @PreAuthorize("hasAuthority('EDIT_OWN_PROFILE')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public CoachReadOnlyDTO updateCurrentCoach(String username, CoachUpdateDTO dto) throws EntityNotFoundException {
        CoachProfile profile = coachProfileRepository.findByUserUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new EntityNotFoundException("Coach", "Coach profile not found for user " + username));

        profile.setDisplayName(dto.displayName());
        profile.setSpecialty(dto.specialty());
        profile.setBio(dto.bio());
        profile.setYearsExperience(dto.yearsExperience());

        return mapper.mapToCoachReadOnlyDTO(profile);
    }
}
