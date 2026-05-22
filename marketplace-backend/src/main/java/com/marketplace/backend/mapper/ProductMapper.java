package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.ProductDto;
import com.marketplace.backend.model.Product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    public ProductDto mapToDto(Product product, List<String> images) {
        return ProductDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .images(images)
                .category(product.getCategory())
                .modifiedAt(product.getModifiedAt())
                .createdAt(product.getCreatedAt())
                .inStock(product.isInStock())
                .author(product.getAuthor())
                .buyerId(product.getBuyerId())
                .productStatus(product.getProductStatus())
                .moderationActionId(product.getModerationActionId())
                .build();
    }
}
