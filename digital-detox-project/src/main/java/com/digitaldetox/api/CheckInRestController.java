package com.digitaldetox.api;

import com.digitaldetox.core.exceptions.EntityAlreadyExistsException;
import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.core.exceptions.ValidationException;
import com.digitaldetox.core.filters.CheckInFilters;
import com.digitaldetox.dto.checkin.CheckInInsertDTO;
import com.digitaldetox.dto.checkin.CheckInReadOnlyDTO;
import com.digitaldetox.service.ICheckInService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans/{planUuid}/check-ins")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class CheckInRestController {

    private final ICheckInService checkInService;

    @PostMapping
    public ResponseEntity<CheckInReadOnlyDTO> createCheckIn(@PathVariable UUID planUuid,
                                                            @Valid @RequestBody CheckInInsertDTO dto,
                                                            BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException, EntityAlreadyExistsException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("CheckIn", "Validation failed", bindingResult);
        }

        CheckInReadOnlyDTO created = checkInService.createCheckIn(planUuid, dto);
        return ResponseEntity
                .created(URI.create("/api/v1/plans/" + planUuid + "/check-ins/" + created.uuid()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<Page<CheckInReadOnlyDTO>> getCheckIns(
            @PathVariable UUID planUuid,
            @ModelAttribute CheckInFilters filters,
            @PageableDefault(page = 0, size = 10, sort = "entryDate,desc") Pageable pageable)
            throws EntityNotFoundException {
        return ResponseEntity.ok(checkInService.getCheckInsPaginated(planUuid, filters, pageable));
    }
}
