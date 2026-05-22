package com.marketplace.backend.model.Review;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {

    @NotBlank(message = "Review text is required")
    @Size(min = 10, max = 256, message = "Review text must be between 10 and 256 characters")
    private String description;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    private Double rating;

    @NotBlank(message = "Target ID is required")
    private String targetId;
}
