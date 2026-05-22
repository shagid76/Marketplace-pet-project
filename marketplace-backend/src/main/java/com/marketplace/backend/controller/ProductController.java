package com.marketplace.backend.controller;

import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.ProductDocumentDto;
import com.marketplace.backend.dto.ProductDto;
import com.marketplace.backend.model.Product.Category;
import com.marketplace.backend.model.Product.CreateProductRequest;
import com.marketplace.backend.model.Product.UpdateProductRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Create, browse, search, and manage product listings")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    private final ProductService productService;

    @Operation(summary = "Get a product by ID")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String productId) {
        ProductDto product = productService.findByProductId(productId);
        return ResponseEntity.ok(product);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Operation(summary = "List all products (admin/moderator only)")
    @GetMapping()
    public ResponseEntity<PageResponseDto<ProductDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "5") int size) {
        PageResponseDto<ProductDto> products = productService.findAll(page, size);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "List all available product categories")
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> categories() {
        List<Category> categories = productService.findAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Get all products listed by a specific user")
    @GetMapping("/author")
    public ResponseEntity<List<ProductDto>> getAllProductsByTargetId(@RequestParam String targetId) {
        List<ProductDto> products = productService.findAllByAuthorId(targetId);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get all products listed by the authenticated user")
    @GetMapping("/me")
    public ResponseEntity<List<ProductDto>> getAllMyProducts() {
        List<ProductDto> products = productService.findAllByAuthorId(SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get all products purchased by the authenticated user")
    @GetMapping("/me/purchases")
    public ResponseEntity<List<ProductDto>> getAllMyPurchases() {
        List<ProductDto> products = productService.findAllByBuyerId(SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Update a product listing (owner only)")
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable String id, @RequestPart("product") @Valid UpdateProductRequest updateProductRequest,
                                                    @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ProductDto product = productService.update(id, images, updateProductRequest, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Create a new product listing with images")
    @PostMapping("")
    public ResponseEntity<ProductDto> create(@RequestPart("product") @Valid CreateProductRequest createProductRequest,
                                             @RequestPart("images") List<MultipartFile> images) {
        ProductDto product = productService.createProduct(createProductRequest, images, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @Operation(summary = "Get the most recently listed products")
    @GetMapping("/new-products")
    public ResponseEntity<List<ProductDto>> getNewProducts(@RequestParam(defaultValue = "10") int limit) {
        List<ProductDto> products = productService.findNewItems(limit);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get all products in a specific category")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable Category category) {
        List<ProductDto> products = productService.findAllByCategory(category);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Full-text search with optional category and price filters (Elasticsearch)")
    @GetMapping("/search")
    public ResponseEntity<List<ProductDocumentDto>> search(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", required = false) Category category,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice
    ) {
        List<ProductDocumentDto> products = productService.search(query, category, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Delete a product listing (owner only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}