package com.marketplace.backend.service;

import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.ReviewDto;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.PageMapper;
import com.marketplace.backend.mapper.ReviewMapper;
import com.marketplace.backend.model.Review.CreateReviewRequest;
import com.marketplace.backend.model.Review.Review;
import com.marketplace.backend.model.Review.ReviewStatus;
import com.marketplace.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final PageMapper pageMapper;

    private Page<ReviewDto> findPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findAll(pageable)
                .map(reviewMapper::mapToDto);
    }

    public PageResponseDto<ReviewDto> findAll(int page, int size) {
        return pageMapper.mapToDto(page, size, findPage(page, size));
    }

    private Review createNew(CreateReviewRequest createReviewRequest, String currentUserId) {
        Review review = Review.builder()
                .id(UUID.randomUUID().toString())
                .description(createReviewRequest.getDescription())
                .rating(createReviewRequest.getRating())
                .lastUpdated(LocalDateTime.now())
                .author(currentUserId)
                .targetId(createReviewRequest.getTargetId())
                .reviewStatus(ReviewStatus.ACTIVE)
                .build();
        return reviewRepository.save(review);
    }

    private Review applyChanges(CreateReviewRequest createReviewRequest, Review review) {
        review.setDescription(createReviewRequest.getDescription());
        review.setRating(createReviewRequest.getRating());
        review.setLastUpdated(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public ReviewDto createOrUpdateReview(CreateReviewRequest createReviewRequest, String currentUserId) {
        Review review = reviewRepository
                .findByAuthorAndTargetId(currentUserId, createReviewRequest.getTargetId())
                .map(existing -> applyChanges(createReviewRequest, existing))
                .orElseGet(() -> createNew(createReviewRequest, currentUserId));
        return reviewMapper.mapToDto(review);
    }

    public Review findById(String id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found"));
    }

    public Double findAverageRating(String targetId) {
        return reviewRepository.findAllByTargetIdAndReviewStatus(targetId, ReviewStatus.ACTIVE).stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public List<ReviewDto> findAllReviewsByTargetId(String targetId) {
        return mapToDto(reviewRepository.findAllByTargetIdAndReviewStatus(targetId, ReviewStatus.ACTIVE));
    }

    private List<ReviewDto> mapToDto(List<Review> reviews) {
        return reviews.stream()
                .map(reviewMapper::mapToDto)
                .toList();
    }

    public ReviewDto findReviewByAuthorAndTargetId(String author, String targetId) {
        return reviewMapper.mapToDto(reviewRepository.findByAuthorAndTargetId(author, targetId)
                .orElseThrow(() -> new NotFoundException("Review not found")));
    }

    public void delete(String reviewId, String currentUserId) {
        Review review = findById(reviewId);
        if (!review.getAuthor().equals(currentUserId)) {
            throw new AccessDeniedException("You are not a review author!");
        }
        reviewRepository.delete(review);
    }
}