package com.marketplace.backend.repository;

import com.marketplace.backend.model.Review.Review;
import com.marketplace.backend.model.Review.ReviewStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findAllByTargetIdAndReviewStatus(String targetId, ReviewStatus status);

    Optional<Review> findByAuthorAndTargetId(String author, String targetId);
}
