package com.digitaldetox.service;

import com.digitaldetox.core.exceptions.EntityAlreadyExistsException;
import com.digitaldetox.core.exceptions.EntityInvalidArgumentException;
import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.dto.coach.CoachReadOnlyDTO;
import com.digitaldetox.dto.coach.CoachRegisterDTO;
import com.digitaldetox.dto.coach.CoachUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface ICoachService {

    CoachReadOnlyDTO register(CoachRegisterDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    CoachReadOnlyDTO getByUuid(UUID uuid) throws EntityNotFoundException;

    List<CoachReadOnlyDTO> getApprovedCoaches();

    List<CoachReadOnlyDTO> getPendingCoaches();

    CoachReadOnlyDTO approveCoach(UUID uuid) throws EntityNotFoundException;

    CoachReadOnlyDTO updateCurrentCoach(String username, CoachUpdateDTO dto) throws EntityNotFoundException;
}
