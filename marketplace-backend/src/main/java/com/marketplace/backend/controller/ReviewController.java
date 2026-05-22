package com.marketplace.backend.controller;

import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.ReviewDto;
import com.marketplace.backend.model.Review.CreateReviewRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Seller ratings and reviews")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(summary = "Create or update a review for a seller")
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@RequestBody @Valid CreateReviewRequest createReviewRequest) {
        ReviewDto review = reviewService.createOrUpdateReview(createReviewRequest, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Operation(summary = "List all reviews (admin/moderator only)")
    @GetMapping()
    public ResponseEntity<PageResponseDto<ReviewDto>> getAllReviews(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "5") int size) {
        PageResponseDto<ReviewDto> reviews = reviewService.findAll(page, size);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get the average rating for a user")
    @GetMapping("/average/{targetId}")
    public ResponseEntity<Double> findAverageRating(@PathVariable String targetId) {
        Double averageRating = reviewService.findAverageRating(targetId);
        return ResponseEntity.ok(averageRating);
    }

    @Operation(summary = "Get the authenticated user's own average rating")
    @GetMapping("/average/me")
    public ResponseEntity<Double> findMyAverageRating() {
        Double myAverageRating = reviewService.findAverageRating(SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.ok(myAverageRating);
    }

    @Operation(summary = "Get all reviews for a specific user")
    @GetMapping("/{targetId}")
    public ResponseEntity<List<ReviewDto>> findReview(@PathVariable String targetId) {
        List<ReviewDto> reviews = reviewService.findAllReviewsByTargetId(targetId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get the authenticated user's review for a specific seller")
    @GetMapping("/my-review")
    public ResponseEntity<ReviewDto> findMyReview(@RequestParam String targetId) {
        ReviewDto review = reviewService.findReviewByAuthorAndTargetId(SecurityUtils.getCurrentUserIdOrThrow(), targetId);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Delete a review (author only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.delete(id, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.noContent().build();
    }

}