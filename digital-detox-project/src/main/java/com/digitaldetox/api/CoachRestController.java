package com.digitaldetox.api;

import com.digitaldetox.core.exceptions.*;
import com.digitaldetox.dto.coach.CoachReadOnlyDTO;
import com.digitaldetox.dto.coach.CoachRegisterDTO;
import com.digitaldetox.dto.coach.CoachUpdateDTO;
import com.digitaldetox.model.User;
import com.digitaldetox.service.ICoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
public class CoachRestController {

    private final ICoachService coachService;

    @PostMapping("/register")
    public ResponseEntity<CoachReadOnlyDTO> register(@Valid @RequestBody CoachRegisterDTO dto,
                                                     BindingResult bindingResult)
            throws ValidationException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Coach", "Validation failed", bindingResult);
        }

        CoachReadOnlyDTO created = coachService.register(dto);
        return ResponseEntity
                .created(URI.create("/api/v1/coaches/" + created.uuid()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<CoachReadOnlyDTO>> getApprovedCoaches() {
        return ResponseEntity.ok(coachService.getApprovedCoaches());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<CoachReadOnlyDTO>> getPendingCoaches() {
        return ResponseEntity.ok(coachService.getPendingCoaches());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CoachReadOnlyDTO> getByUuid(@PathVariable UUID uuid)
            throws EntityNotFoundException {
        return ResponseEntity.ok(coachService.getByUuid(uuid));
    }

    @PatchMapping("/{uuid}/approve")
    public ResponseEntity<CoachReadOnlyDTO> approveCoach(@PathVariable UUID uuid)
            throws EntityNotFoundException {
        return ResponseEntity.ok(coachService.approveCoach(uuid));
    }

    @PutMapping("/me")
    public ResponseEntity<CoachReadOnlyDTO> updateCurrentCoach(@AuthenticationPrincipal User user,
                                                               @Valid @RequestBody CoachUpdateDTO dto,
                                                               BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Coach", "Validation failed", bindingResult);
        }

        return ResponseEntity.ok(coachService.updateCurrentCoach(user.getUsername(), dto));
    }
}
