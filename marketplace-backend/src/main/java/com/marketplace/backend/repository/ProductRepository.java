package com.marketplace.backend.repository;

import com.marketplace.backend.model.Product.Category;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.Product.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findAByAuthorAndModerationActionIdIsNull(String author);
    List<Product> findByCategoryAndInStockTrueAndLockedAtIsNullAndProductStatus(Category category, ProductStatus status);
    List<Product> findAllByBuyerId(String buyerId);
    List<Product> findAllByProductStatusAndLockedAtBefore(ProductStatus status, LocalDateTime timeout);
    List<Product> findByInStockTrueAndLockedAtIsNullAndProductStatusOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);
}