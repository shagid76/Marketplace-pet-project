package com.marketplace.backend.model.Review;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @Id
    private String id;
    private String description;
    private Double rating;
    @LastModifiedDate
    private LocalDateTime lastUpdated;
    @NotNull(message = "")
    private String author;
    @NotNull(message = "")
    private String targetId;

    private ReviewStatus reviewStatus;
    private String moderationActionId;
}