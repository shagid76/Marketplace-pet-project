package com.marketplace.backend.model.Product;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "products")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    private String id;
    @Version
    private Long version;
    private String title;
    private String description;
    @Positive
    private BigDecimal price;
    private List<String> images = new ArrayList<>();
    private Category category;
    @LastModifiedDate
    private LocalDateTime modifiedAt;
    @CreatedDate
    private LocalDateTime createdAt;
    private boolean inStock;
    private String buyerId;
    private String author;
    private LocalDateTime lockedAt;
    private String lockedByPaymentId;
    private ProductStatus productStatus;
    private String moderationActionId;
}