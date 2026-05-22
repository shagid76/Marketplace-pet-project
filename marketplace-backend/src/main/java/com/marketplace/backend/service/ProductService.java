package com.marketplace.backend.service;

import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.ProductDocumentDto;
import com.marketplace.backend.dto.ProductDto;
import com.marketplace.backend.exception.ForbiddenException;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.PageMapper;
import com.marketplace.backend.mapper.ProductDocumentMapper;
import com.marketplace.backend.mapper.ProductMapper;
import com.marketplace.backend.model.Product.*;
import com.marketplace.backend.repository.ProductRepository;
import com.marketplace.backend.repository.ProductSearchRepository;
import com.marketplace.backend.model.PromoCode.PromoCodeUsage;
import com.marketplace.backend.repository.PromoCodeUsageRepository;
import com.marketplace.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;
    private final ElasticsearchOperations elasticsearchTemplate;
    private final MinioService minioService;
    private final ProductMapper productMapper;
    private final ProductDocumentMapper productDocumentMapper;
    private final PageMapper pageMapper;

    public Page<ProductDto> findPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable)
                .map(product -> productMapper.mapToDto(product, minioService.buildUrlImage(product.getImages())));
    }

    public PageResponseDto<ProductDto> findAll(int page, int size) {
        return pageMapper.mapToDto(page, size, findPage(page, size));
    }

    private Product findById(String id) {
        return productRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Item not found with id: " + id));
    }

    public ProductDto findByProductId(String id) {
        Product product = findById(id);
        checkBan(product);
        return productMapper.mapToDto(product, minioService.buildUrlImage(product.getImages()));
    }

    private void checkBan(Product product) {
        if (product.getProductStatus().equals(ProductStatus.BANNED)) {
            throw new ForbiddenException("This product is banned!");
        }
    }

    public List<Category> findAllCategories() {
        return Arrays.asList(Category.values());
    }

    public List<ProductDto> findNewItems(int limit) {
        return productRepository.findByInStockTrueAndLockedAtIsNullAndProductStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE, PageRequest.of(0, limit)).stream()
                .map(product -> productMapper.mapToDto(product, minioService.buildUrlImage(product.getImages())))
                .toList();
    }

    public List<ProductDto> findAllByAuthorId(String authorId) {
        return productRepository.findAByAuthorAndModerationActionIdIsNull(authorId).stream()
                .map(product -> productMapper.mapToDto(product, minioService.buildUrlImage(product.getImages())))
                .toList();
    }

    public List<ProductDto> findAllByCategory(Category category) {
        return productRepository.findByCategoryAndInStockTrueAndLockedAtIsNullAndProductStatus(category, ProductStatus.ACTIVE).stream()
                .map(product -> productMapper.mapToDto(product, minioService.buildUrlImage(product.getImages())))
                .toList();
    }

    public List<ProductDto> findAllByBuyerId(String buyerId) {
        return productRepository.findAllByBuyerId(buyerId).stream()
                .map(product -> productMapper.mapToDto(product, minioService.buildUrlImage(product.getImages())))
                .toList();
    }

    private Product create(CreateProductRequest createProductRequest, List<MultipartFile> images, String authorId) {
        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .title(createProductRequest.getTitle())
                .description(createProductRequest.getDescription())
                .price(createProductRequest.getPrice())
                .images(minioService.upload(images))
                .category(createProductRequest.getCategory())
                .createdAt(LocalDateTime.now())
                .productStatus(ProductStatus.ACTIVE)
                .inStock(true)
                .author(authorId)
                .build();
        return productRepository.save(product);
    }

    private ProductDocument findProductDocumentById(String id) {
        return productSearchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    public ProductDto createProduct(CreateProductRequest createProductRequest, List<MultipartFile> images, String authorId) {
        Product product = create(createProductRequest, images, authorId);
        ProductDocument productDocument = productDocumentMapper.mapToDocument(product, minioService.buildUrlImage(product.getImages()));
        productSearchRepository.save(productDocument);
        log.info("Product created: id={}, title={}, author={}", product.getId(), product.getTitle(), authorId);
        return productMapper.mapToDto(product, minioService.buildUrlImage(product.getImages()));
    }

    public List<ProductDocumentDto> search(String query, Category category, BigDecimal minPrice, BigDecimal maxPrice) {
        Criteria criteria = Criteria.where("inStock").is(true)
                .and("banned").is(false)
                .and("locked").is(false);

        if (query != null && !query.isEmpty()) {
            Criteria searchBlock = new Criteria("title").contains(query);
            criteria = criteria.and(searchBlock);
        }

        if (category != null) {
            criteria = criteria.and(new Criteria("category").is(category.name()));
        }

        if (minPrice != null || maxPrice != null) {
            if (minPrice != null) criteria = criteria.and(new Criteria("price").greaterThanEqual(minPrice));
            if (maxPrice != null) criteria = criteria.and(new Criteria("price").lessThanEqual(maxPrice));
        }

        Query searchQuery = new CriteriaQuery(criteria);
        SearchHits<ProductDocument> hits = elasticsearchTemplate.search(searchQuery, ProductDocument.class);

        return hits.stream()
                .map(hit -> productDocumentMapper.mapDocumentToDto(hit.getContent()))
                .toList();
    }

    public ProductDto update(String id, List<MultipartFile> images, UpdateProductRequest updateProductRequest,
                             String currentUserId) {
        Product product = findById(id);
        ProductDocument productDocument = findProductDocumentById(id);

        validateProduct(product, productDocument, currentUserId);
        List<String> oldImages = product.getImages();
        List<String> imagesToKeep = createImagesToKeep(updateProductRequest);
        updateProduct(product, updateProductRequest, images, imagesToKeep);
        updateProductDocument(productDocument, product);
        deleteRemovedImages(oldImages, imagesToKeep);
        log.info("Product updated: id={}, by={}", id, currentUserId);
        return productMapper.mapToDto(product, minioService.buildUrlImage(product.getImages()));
    }

    private void deleteRemovedImages(
            List<String> oldImages,
            List<String> imagesToKeep
    ) {

        for (String oldImage : oldImages) {
            if (!imagesToKeep.contains(oldImage)) {
                minioService.delete(oldImage);
            }
        }
    }

    private List<String> createImagesToKeep(UpdateProductRequest updateProductRequest) {
        return updateProductRequest.getExistingImages() != null
                ? updateProductRequest.getExistingImages().stream()
                .map(url -> url.substring(url.lastIndexOf("/") + 1))
                .toList()
                : new ArrayList<>();
    }

    private void updateProduct(Product product, UpdateProductRequest updateProductRequest, List<MultipartFile> images,
                               List<String> imagesToKeep) {
        product.setTitle(updateProductRequest.getTitle());
        product.setDescription(updateProductRequest.getDescription());
        product.setPrice(updateProductRequest.getPrice());
        product.setImages(minioService.upload(images, imagesToKeep));
        product.setCategory(updateProductRequest.getCategory());
        productRepository.save(product);
    }

    private void updateProductDocument(ProductDocument productDocument, Product product) {
        productDocument.setTitle(product.getTitle());
        productDocument.setDescription(product.getDescription());
        productDocument.setPrice(product.getPrice());
        productDocument.setCategory(product.getCategory());
        productDocument.setImages(minioService.buildUrlImage(product.getImages()));
        productSearchRepository.save(productDocument);
    }

    private void validateProduct(Product product, ProductDocument productDocument, String currentUserId) {
        if (!productDocument.getAuthor().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner");
        }

        if (!product.getAuthor().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner");
        }
    }

    public void delete(String id) {
        Product product = findById(id);
        ProductDocument productDocument = findProductDocumentById(id);
        String currentUserId = SecurityUtils.getCurrentUserIdOrThrow();
        if (!product.getAuthor().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the owner");
        }
        productRepository.delete(product);
        productSearchRepository.delete(productDocument);
        log.info("Product deleted: id={}, by={}", id, currentUserId);
    }

    @Transactional
    public void buy(List<String> productIdList, String buyerId, String promoCodeId) {
        List<Product> products = productRepository.findAllById(productIdList);

        Map<String, ProductDocument> documentMap = productSearchRepository.findAllById(productIdList)
                .stream()
                .collect(Collectors.toMap(ProductDocument::getId, doc -> doc));

        List<Product> productsToSave = new ArrayList<>();
        List<ProductDocument> documentsToSave = new ArrayList<>();

        for (Product product : products) {
            if (!product.isInStock() || product.getAuthor().equals(buyerId)) {
                continue;
            }
            if (product.getProductStatus().equals(ProductStatus.BANNED)) {
                throw new ForbiddenException("Action impossible: product is banned.");
            }

            product.setBuyerId(buyerId);
            product.setInStock(false);
            product.setProductStatus(ProductStatus.ACTIVE);
            product.setLockedByPaymentId(null);
            product.setLockedAt(null);
            productsToSave.add(product);

            ProductDocument doc = documentMap.get(product.getId());
            if (doc != null) {
                doc.setInStock(false);
                doc.setLocked(false);
                documentsToSave.add(doc);
            }

        }
        if (promoCodeId != null) {
            promoCodeUsageRepository.save(createPromoCodeUsage(buyerId, promoCodeId));
        }

        if (!productsToSave.isEmpty()) {
            productRepository.saveAll(productsToSave);
        }
        if (!documentsToSave.isEmpty()) {
            productSearchRepository.saveAll(documentsToSave);
            log.info("Purchase completed: products={}, buyer={}", productIdList, buyerId);
        }
    }

    private PromoCodeUsage createPromoCodeUsage(String userId, String promoCodeId) {
        return PromoCodeUsage.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .promoCodeId(promoCodeId)
                .build();
    }
}
