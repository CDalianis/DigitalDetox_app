package com.digitaldetox.api;

import com.digitaldetox.core.exceptions.EntityInvalidArgumentException;
import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.core.exceptions.ValidationException;
import com.digitaldetox.core.filters.DetoxPlanFilters;
import com.digitaldetox.dto.plan.DetoxPlanInsertDTO;
import com.digitaldetox.dto.plan.DetoxPlanReadOnlyDTO;
import com.digitaldetox.dto.plan.DetoxPlanUpdateDTO;
import com.digitaldetox.model.User;
import com.digitaldetox.model.enums.DetoxPlanStatus;
import com.digitaldetox.service.IDetoxPlanService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class DetoxPlanRestController {

    private final IDetoxPlanService detoxPlanService;

    @PostMapping
    public ResponseEntity<DetoxPlanReadOnlyDTO> createPlan(@AuthenticationPrincipal User user,
                                                           @Valid @RequestBody DetoxPlanInsertDTO dto,
                                                           BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("DetoxPlan", "Validation failed", bindingResult);
        }

        DetoxPlanReadOnlyDTO created = detoxPlanService.createPlan(user.getUsername(), dto);
        return ResponseEntity
                .created(URI.create("/api/v1/plans/" + created.uuid()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<Page<DetoxPlanReadOnlyDTO>> getPlans(
            @AuthenticationPrincipal User user,
            @ModelAttribute DetoxPlanFilters filters,
            @PageableDefault(page = 0, size = 10, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(detoxPlanService.getPlansPaginated(user.getUsername(), filters, pageable));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<DetoxPlanReadOnlyDTO> getByUuid(@PathVariable UUID uuid)
            throws EntityNotFoundException {
        return ResponseEntity.ok(detoxPlanService.getByUuid(uuid));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<DetoxPlanReadOnlyDTO> updatePlan(@PathVariable UUID uuid,
                                                           @Valid @RequestBody DetoxPlanUpdateDTO dto,
                                                           BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("DetoxPlan", "Validation failed", bindingResult);
        }

        if (!uuid.equals(dto.uuid())) {
            throw new EntityNotFoundException("DetoxPlan", "Path uuid does not match body uuid");
        }

        return ResponseEntity.ok(detoxPlanService.updatePlan(dto));
    }

    @PatchMapping("/{uuid}/status")
    public ResponseEntity<DetoxPlanReadOnlyDTO> updateStatus(@PathVariable UUID uuid,
                                                             @RequestParam DetoxPlanStatus status)
            throws EntityNotFoundException {
        return ResponseEntity.ok(detoxPlanService.updateStatus(uuid, status));
    }
}
