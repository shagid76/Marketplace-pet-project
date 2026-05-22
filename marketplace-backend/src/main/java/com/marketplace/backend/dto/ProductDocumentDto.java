package com.marketplace.backend.dto;

import com.marketplace.backend.model.Product.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDocumentDto {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private Category category;
    private boolean inStock;
    private boolean locked;
    private boolean banned;
    private List<String> images;
    private String author;
}
