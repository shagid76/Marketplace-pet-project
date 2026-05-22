package com.marketplace.backend.dto;

import com.marketplace.backend.model.Review.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {
    private String id;
    private String description;
    private Double rating;
    private LocalDateTime lastUpdated;
    private String author;
    private String targetId;
    private ReviewStatus reviewStatus;
    private String moderationActionId;
}