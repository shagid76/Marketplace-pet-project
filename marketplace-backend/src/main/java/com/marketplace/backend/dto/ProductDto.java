package com.marketplace.backend.dto;

import com.marketplace.backend.model.Product.Category;
import com.marketplace.backend.model.Product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private List<String> images = new ArrayList<>();
    private Category category;
    private LocalDateTime modifiedAt;
    private LocalDateTime createdAt;
    private boolean inStock;
    private String buyerId;
    private String author;
    private ProductStatus productStatus;
    private String moderationActionId;
}