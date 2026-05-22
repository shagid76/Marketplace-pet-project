package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.ProductDocumentDto;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.Product.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductDocumentMapper {
    public ProductDocumentDto mapDocumentToDto(ProductDocument productDocument) {
        return ProductDocumentDto.builder()
                .id(productDocument.getId())
                .title(productDocument.getTitle())
                .description(productDocument.getDescription())
                .category(productDocument.getCategory())
                .price(productDocument.getPrice())
                .inStock(productDocument.isInStock())
                .locked(productDocument.isLocked())
                .banned(productDocument.isBanned())
                .images(productDocument.getImages())
                .author(productDocument.getAuthor())
                .build();
    }

    public ProductDocument mapToDocument(Product product, List<String> images) {
        return ProductDocument.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .inStock(product.isInStock())
                .locked(false)
                .banned(false)
                .category(product.getCategory())
                .images(images)
                .author(product.getAuthor())
                .build();
    }
}
