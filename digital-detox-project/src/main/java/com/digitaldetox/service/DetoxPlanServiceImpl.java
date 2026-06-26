package com.digitaldetox.service;

import com.digitaldetox.core.exceptions.EntityInvalidArgumentException;
import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.core.filters.DetoxPlanFilters;
import com.digitaldetox.specification.DetoxPlanSpecification;
import com.digitaldetox.dto.plan.DetoxPlanInsertDTO;
import com.digitaldetox.dto.plan.DetoxPlanReadOnlyDTO;
import com.digitaldetox.dto.plan.DetoxPlanUpdateDTO;
import com.digitaldetox.mapper.Mapper;
import com.digitaldetox.model.CoachProfile;
import com.digitaldetox.model.DetoxPlan;
import com.digitaldetox.model.MemberProfile;
import com.digitaldetox.model.enums.DetoxPlanStatus;
import com.digitaldetox.repository.CoachProfileRepository;
import com.digitaldetox.repository.DetoxPlanRepository;
import com.digitaldetox.repository.MemberProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetoxPlanServiceImpl implements IDetoxPlanService {

    private final DetoxPlanRepository detoxPlanRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final CoachProfileRepository coachProfileRepository;
    private final Mapper mapper;

    @Override
    @PreAuthorize("hasAuthority('CREATE_PLAN') and @securityService.isApprovedCoach(authentication)")
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityInvalidArgumentException.class})
    public DetoxPlanReadOnlyDTO createPlan(String coachUsername, DetoxPlanInsertDTO dto)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        CoachProfile coach = coachProfileRepository.findByUserUsernameAndDeletedFalse(coachUsername)
                .orElseThrow(() -> new EntityNotFoundException("Coach", "Coach profile not found"));

        if (!coach.isApproved()) {
            throw new EntityInvalidArgumentException("Coach", "Coach account is not approved yet");
        }

        MemberProfile member = memberProfileRepository.findByUuidAndDeletedFalse(dto.memberProfileUuid())
                .orElseThrow(() -> new EntityNotFoundException("Member", "Member with uuid=" + dto.memberProfileUuid() + " not found"));

        DetoxPlan plan = mapper.mapToDetoxPlanEntity(dto);
        plan.setMemberProfile(member);
        plan.setCoachProfile(coach);

        detoxPlanRepository.save(plan);
        log.info("Detox plan created uuid={} for member={}", plan.getUuid(), member.getUuid());
        return mapper.mapToDetoxPlanReadOnlyDTO(plan);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_PLANS') or (hasAuthority('VIEW_OWN_PLANS') and @securityService.isPlanParticipant(#uuid, authentication))")
    @Transactional(readOnly = true)
    public DetoxPlanReadOnlyDTO getByUuid(UUID uuid) throws EntityNotFoundException {
        DetoxPlan plan = detoxPlanRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new EntityNotFoundException("DetoxPlan", "Plan with uuid=" + uuid + " not found"));
        return mapper.mapToDetoxPlanReadOnlyDTO(plan);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_PLANS') or hasAuthority('VIEW_OWN_PLANS')")
    @Transactional(readOnly = true)
    public List<DetoxPlanReadOnlyDTO> getPlansForCurrentUser(String username) {
        return memberProfileRepository.findByUserUsernameAndDeletedFalse(username)
                .map(member -> detoxPlanRepository.findAllByMemberProfileUuidAndDeletedFalse(member.getUuid())
                        .stream().map(mapper::mapToDetoxPlanReadOnlyDTO).toList())
                .orElseGet(() -> coachProfileRepository.findByUserUsernameAndDeletedFalse(username)
                        .map(coach -> detoxPlanRepository.findAllByCoachProfileUuidAndDeletedFalse(coach.getUuid())
                                .stream().map(mapper::mapToDetoxPlanReadOnlyDTO).toList())
                        .orElse(detoxPlanRepository.findAllByDeletedFalse().stream()
                                .map(mapper::mapToDetoxPlanReadOnlyDTO).toList()));
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_PLANS') or hasAuthority('VIEW_OWN_PLANS')")
    @Transactional(readOnly = true)
    public Page<DetoxPlanReadOnlyDTO> getPlansPaginated(String username, DetoxPlanFilters filters, Pageable pageable) {
        DetoxPlanFilters scopedFilters = applyRoleScope(username, filters);
        return detoxPlanRepository.findAll(DetoxPlanSpecification.build(scopedFilters), pageable)
                .map(mapper::mapToDetoxPlanReadOnlyDTO);
    }

    private DetoxPlanFilters applyRoleScope(String username, DetoxPlanFilters filters) {
        final DetoxPlanFilters effectiveFilters = filters == null
                ? DetoxPlanFilters.builder().build()
                : filters;

        return memberProfileRepository.findByUserUsernameAndDeletedFalse(username)
                .map(member -> DetoxPlanFilters.builder()
                        .memberProfileUuid(member.getUuid())
                        .status(effectiveFilters.getStatus())
                        .title(effectiveFilters.getTitle())
                        .focusArea(effectiveFilters.getFocusArea())
                        .build())
                .orElseGet(() -> coachProfileRepository.findByUserUsernameAndDeletedFalse(username)
                        .map(coach -> DetoxPlanFilters.builder()
                                .coachProfileUuid(coach.getUuid())
                                .status(effectiveFilters.getStatus())
                                .title(effectiveFilters.getTitle())
                                .focusArea(effectiveFilters.getFocusArea())
                                .build())
                        .orElse(effectiveFilters));
    }

    @Override
    @PreAuthorize("hasAuthority('EDIT_PLAN')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public DetoxPlanReadOnlyDTO updatePlan(DetoxPlanUpdateDTO dto) throws EntityNotFoundException {
        DetoxPlan plan = detoxPlanRepository.findByUuidAndDeletedFalse(dto.uuid())
                .orElseThrow(() -> new EntityNotFoundException("DetoxPlan", "Plan with uuid=" + dto.uuid() + " not found"));
        mapper.updateDetoxPlan(plan, dto);
        return mapper.mapToDetoxPlanReadOnlyDTO(plan);
    }

    @Override
    @PreAuthorize("hasAuthority('EDIT_PLAN')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public DetoxPlanReadOnlyDTO updateStatus(UUID uuid, DetoxPlanStatus status) throws EntityNotFoundException {
        DetoxPlan plan = detoxPlanRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new EntityNotFoundException("DetoxPlan", "Plan with uuid=" + uuid + " not found"));
        plan.setStatus(status);
        return mapper.mapToDetoxPlanReadOnlyDTO(plan);
    }
}
