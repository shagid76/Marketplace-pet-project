package com.marketplace.backend.service;

import com.marketplace.backend.dto.ReviewDto;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.PageMapper;
import com.marketplace.backend.mapper.ReviewMapper;
import com.marketplace.backend.model.Review.CreateReviewRequest;
import com.marketplace.backend.model.Review.Review;
import com.marketplace.backend.model.Review.ReviewStatus;
import com.marketplace.backend.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock ReviewRepository reviewRepository;
	@Mock ReviewMapper reviewMapper;
	@Mock PageMapper pageMapper;

	@InjectMocks ReviewService reviewService;

	@Test
	void createOrUpdateReview_whenNoExisting_createsNew() {
		CreateReviewRequest req = new CreateReviewRequest();
		req.setDescription("Great seller");
		req.setRating(5.0);
		req.setTargetId("seller-1");

		when(reviewRepository.findByAuthorAndTargetId("user-1", "seller-1"))
				.thenReturn(Optional.empty());
		when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
		when(reviewMapper.mapToDto(any(Review.class))).thenReturn(new ReviewDto());

		reviewService.createOrUpdateReview(req, "user-1");

		verify(reviewRepository).save(any(Review.class));
	}

	@Test
	void createOrUpdateReview_whenExisting_updatesIt() {
		CreateReviewRequest req = new CreateReviewRequest();
		req.setDescription("Updated text");
		req.setRating(3.0);
		req.setTargetId("seller-1");

		Review existing = Review.builder()
				.id("r-1").author("user-1").targetId("seller-1")
				.rating(5.0).description("old")
				.reviewStatus(ReviewStatus.ACTIVE)
				.build();

		when(reviewRepository.findByAuthorAndTargetId("user-1", "seller-1"))
				.thenReturn(Optional.of(existing));
		when(reviewRepository.save(existing)).thenReturn(existing);
		when(reviewMapper.mapToDto(existing)).thenReturn(new ReviewDto());

		reviewService.createOrUpdateReview(req, "user-1");

		assertThat(existing.getDescription()).isEqualTo("Updated text");
		assertThat(existing.getRating()).isEqualTo(3.0);
	}

	@Test
	void findAverageRating_onlyActiveReviewsCounted() {
		when(reviewRepository.findAllByTargetIdAndReviewStatus("seller-1", ReviewStatus.ACTIVE))
				.thenReturn(List.of(
						Review.builder().rating(5.0).build(),
						Review.builder().rating(3.0).build(),
						Review.builder().rating(4.0).build()
				));

		Double avg = reviewService.findAverageRating("seller-1");

		assertThat(avg).isEqualTo(4.0);
	}

	@Test
	void findAverageRating_noReviews_returnsZero() {
		when(reviewRepository.findAllByTargetIdAndReviewStatus("seller-1", ReviewStatus.ACTIVE))
				.thenReturn(List.of());
		assertThat(reviewService.findAverageRating("seller-1")).isZero();
	}

	@Test
	void delete_whenOwner_deletesReview() {
		Review review = Review.builder().id("r-1").author("user-1").build();
		when(reviewRepository.findById("r-1")).thenReturn(Optional.of(review));

		reviewService.delete("r-1", "user-1");

		verify(reviewRepository).delete(review);
	}

	@Test
	void delete_whenNotOwner_throwsAccessDenied() {
		Review review = Review.builder().id("r-1").author("user-1").build();
		when(reviewRepository.findById("r-1")).thenReturn(Optional.of(review));

		assertThatThrownBy(() -> reviewService.delete("r-1", "user-2"))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("not a review author");

		verify(reviewRepository, never()).delete(any());
	}

	@Test
	void findById_notFound_throws() {
		when(reviewRepository.findById("ghost")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> reviewService.findById("ghost"))
				.isInstanceOf(NotFoundException.class);
	}
}
