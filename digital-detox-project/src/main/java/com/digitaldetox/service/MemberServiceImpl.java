package com.digitaldetox.service;

import com.digitaldetox.core.exceptions.EntityAlreadyExistsException;
import com.digitaldetox.core.exceptions.EntityInvalidArgumentException;
import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.dto.member.MemberReadOnlyDTO;
import com.digitaldetox.dto.member.MemberRegisterDTO;
import com.digitaldetox.dto.member.MemberUpdateDTO;
import com.digitaldetox.mapper.Mapper;
import com.digitaldetox.model.MemberProfile;
import com.digitaldetox.model.Role;
import com.digitaldetox.model.User;
import com.digitaldetox.repository.MemberProfileRepository;
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
public class MemberServiceImpl implements IMemberService {

    private static final String MEMBER_ROLE = "MEMBER";

    private final MemberProfileRepository memberProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = {EntityAlreadyExistsException.class, EntityInvalidArgumentException.class})
    public MemberReadOnlyDTO register(MemberRegisterDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (userRepository.findByUsernameAndDeletedFalse(dto.user().username()).isPresent()) {
            throw new EntityAlreadyExistsException("Username", "User with username " + dto.user().username() + " already exists");
        }
        if (userRepository.findByEmailAndDeletedFalse(dto.user().email()).isPresent()) {
            throw new EntityAlreadyExistsException("Email", "User with email " + dto.user().email() + " already exists");
        }

        Role role = roleRepository.findByName(MEMBER_ROLE)
                .orElseThrow(() -> new EntityInvalidArgumentException("Role", "Role " + MEMBER_ROLE + " not found"));

        MemberProfile profile = mapper.mapToMemberProfile(dto);
        User user = profile.getUser();
        user.setPassword(passwordEncoder.encode(dto.user().password()));
        role.addUser(user);

        memberProfileRepository.save(profile);
        log.info("Member registered with username={}", user.getUsername());
        return mapper.mapToMemberReadOnlyDTO(profile);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_MEMBERS')")
    @Transactional(readOnly = true)
    public List<MemberReadOnlyDTO> listMembers() {
        return memberProfileRepository.findAllByDeletedFalseOrderByDisplayNameAsc()
                .stream()
                .map(mapper::mapToMemberReadOnlyDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_MEMBERS') or @securityService.isOwnMemberProfile(#uuid, authentication)")
    @Transactional(readOnly = true)
    public MemberReadOnlyDTO getByUuid(UUID uuid) throws EntityNotFoundException {
        MemberProfile profile = memberProfileRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Member", "Member with uuid=" + uuid + " not found"));
        return mapper.mapToMemberReadOnlyDTO(profile);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_OWN_PROFILE')")
    @Transactional(readOnly = true)
    public MemberReadOnlyDTO getCurrentMember(String username) throws EntityNotFoundException {
        MemberProfile profile = memberProfileRepository.findByUserUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new EntityNotFoundException("Member", "Member profile not found for user " + username));
        return mapper.mapToMemberReadOnlyDTO(profile);
    }

    @Override
    @PreAuthorize("hasAuthority('EDIT_OWN_PROFILE')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public MemberReadOnlyDTO updateCurrentMember(String username, MemberUpdateDTO dto) throws EntityNotFoundException {
        MemberProfile profile = memberProfileRepository.findByUserUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new EntityNotFoundException("Member", "Member profile not found for user " + username));

        profile.setDisplayName(dto.displayName());
        profile.setTimezone(dto.timezone());
        profile.setMainGoal(dto.mainGoal());
        profile.setNotes(dto.notes());

        return mapper.mapToMemberReadOnlyDTO(profile);
    }
}
