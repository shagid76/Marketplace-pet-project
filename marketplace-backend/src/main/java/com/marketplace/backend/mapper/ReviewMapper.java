package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.ReviewDto;
import com.marketplace.backend.model.Review.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    public ReviewDto mapToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .description(review.getDescription())
                .rating(review.getRating())
                .lastUpdated(review.getLastUpdated())
                .author(review.getAuthor())
                .targetId(review.getTargetId())
                .reviewStatus(review.getReviewStatus())
                .moderationActionId(review.getModerationActionId())
                .build();
    }
}
