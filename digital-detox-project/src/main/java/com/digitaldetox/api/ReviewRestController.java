package com.digitaldetox.api;

import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.core.exceptions.ValidationException;
import com.digitaldetox.dto.review.WeeklyReviewInsertDTO;
import com.digitaldetox.dto.review.WeeklyReviewReadOnlyDTO;
import com.digitaldetox.model.User;
import com.digitaldetox.service.IReviewService;
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
@RequestMapping("/api/v1/plans/{planUuid}/reviews")
@RequiredArgsConstructor
public class ReviewRestController {

    private final IReviewService reviewService;

    @PostMapping
    public ResponseEntity<WeeklyReviewReadOnlyDTO> createReview(@PathVariable UUID planUuid,
                                                                @AuthenticationPrincipal User user,
                                                                @Valid @RequestBody WeeklyReviewInsertDTO dto,
                                                                BindingResult bindingResult)
            throws ValidationException, EntityNotFoundException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Review", "Validation failed", bindingResult);
        }

        WeeklyReviewReadOnlyDTO created = reviewService.createReview(user.getUsername(), planUuid, dto);
        return ResponseEntity
                .created(URI.create("/api/v1/plans/" + planUuid + "/reviews/" + created.uuid()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<WeeklyReviewReadOnlyDTO>> getReviews(@PathVariable UUID planUuid)
            throws EntityNotFoundException {
        return ResponseEntity.ok(reviewService.getReviewsByPlan(planUuid));
    }
}
