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
import com.marketplace.backend.model.PromoCode.PromoCodeUsage;
import com.marketplace.backend.repository.ProductRepository;
import com.marketplace.backend.repository.ProductSearchRepository;
import com.marketplace.backend.repository.PromoCodeUsageRepository;
import com.marketplace.backend.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductSearchRepository productSearchRepository;
    @Mock PromoCodeUsageRepository promoCodeUsageRepository;
    @Mock ElasticsearchOperations elasticsearchTemplate;
    @Mock MinioService minioService;
    @Mock ProductMapper productMapper;
    @Mock ProductDocumentMapper productDocumentMapper;
    @Mock PageMapper pageMapper;

    @InjectMocks ProductService productService;

    private MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void setUp() {
        securityUtils = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    private Product activeProduct(String id, String author) {
        return Product.builder()
                .id(id)
                .title("Title " + id)
                .description("Desc")
                .price(BigDecimal.TEN)
                .images(new ArrayList<>())
                .inStock(true)
                .productStatus(ProductStatus.ACTIVE)
                .author(author)
                .category(Category.ELECTRONICS)
                .build();
    }

    private ProductDocument productDocument(String id, String author) {
        return ProductDocument.builder().id(id).author(author).build();
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsMappedPage() {
        Product product = activeProduct("p-1", "author-1");
        Page<Product> page = new PageImpl<>(List.of(product));
        ProductDto dto = new ProductDto();
        dto.setId("p-1");

        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(minioService.buildUrlImage(anyList())).thenReturn(List.of());
        when(productMapper.mapToDto(any(), any())).thenReturn(dto);
        when(pageMapper.mapToDto(anyInt(), anyInt(), any())).thenReturn(new PageResponseDto<>());

        PageResponseDto<ProductDto> result = productService.findAll(0, 5);

        assertThat(result).isNotNull();
    }

    // ── findByProductId ───────────────────────────────────────────────────────

    @Test
    void findByProductId_found_returnsDto() {
        Product product = activeProduct("p-1", "a-1");
        ProductDto dto = ProductDto.builder().id("p-1").build();

        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(minioService.buildUrlImage(anyList())).thenReturn(List.of());
        when(productMapper.mapToDto(any(), any())).thenReturn(dto);

        ProductDto result = productService.findByProductId("p-1");

        assertThat(result.getId()).isEqualTo("p-1");
    }

    @Test
    void findByProductId_notFound_throwsNotFoundException() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findByProductId("missing"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByProductId_bannedProduct_throwsForbidden() {
        Product product = activeProduct("p-1", "a-1");
        product.setProductStatus(ProductStatus.BANNED);

        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.findByProductId("p-1"))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── findAllCategories ────────────────────────────────────────────────────

    @Test
    void findAllCategories_returnsAllEnumValues() {
        List<Category> categories = productService.findAllCategories();
        assertThat(categories).containsExactlyInAnyOrder(Category.values());
    }

    // ── findNewItems ─────────────────────────────────────────────────────────

    @Test
    void findNewItems_returnsProductList() {
        Product product = activeProduct("p-1", "a-1");
        ProductDto dto = ProductDto.builder().id("p-1").build();

        when(productRepository.findByInStockTrueAndLockedAtIsNullAndProductStatusOrderByCreatedAtDesc(
                eq(ProductStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(List.of(product));
        when(minioService.buildUrlImage(anyList())).thenReturn(List.of());
        when(productMapper.mapToDto(any(), any())).thenReturn(dto);

        List<ProductDto> result = productService.findNewItems(5);

        assertThat(result).hasSize(1);
    }

    // ── findAllByAuthorId ────────────────────────────────────────────────────

    @Test
    void findAllByAuthorId_returnsList() {
        Product product = activeProduct("p-1", "author-1");
        ProductDto dto = ProductDto.builder().id("p-1").build();

        when(productRepository.findAByAuthorAndModerationActionIdIsNull("author-1"))
                .thenReturn(List.of(product));
        when(minioService.buildUrlImage(anyList())).thenReturn(List.of());
        when(productMapper.mapToDto(any(), any())).thenReturn(dto);

        List<ProductDto> result = productService.findAllByAuthorId("author-1");

        assertThat(result).hasSize(1);
    }

    // ── findAllByCategory ────────────────────────────────────────────────────

    @Test
    void findAllByCategory_returnsList() {
        Product product = activeProduct("p-1", "a-1");
        ProductDto dto = ProductDto.builder().id("p-1").build();

        when(productRepository.findByCategoryAndInStockTrueAndLockedAtIsNullAndProductStatus(
                eq(Category.ELECTRONICS), eq(ProductStatus.ACTIVE)))
                .thenReturn(List.of(product));
        when(minioService.buildUrlImage(anyList())).thenReturn(List.of());
        when(productMapper.mapToDto(any(), any())).thenReturn(dto);

        List<ProductDto> result = productService.findAllByCategory(Category.ELECTRONICS);
        assertThat(result).hasSize(1);
    }

    // ── findAllByBuyerId ─────────────────────────────────────────────────────

    @Test
    void findAllByBuyerId_returnsList() {
        Product product = activeProduct("p-1", "a-1");
        product.setBuyerId("buyer-1");
        ProductDto dto = ProductDto.builder().id("p-1").build();

        when(productRepository.findAllByBuyerId("buyer-1")).thenReturn(List.of(product));
        when(minioService.buildUrlImage(anyList())).thenReturn(List.of());
        when(productMapper.mapToDto(any(), any())).thenReturn(dto);

        List<ProductDto> result = productService.findAllByBuyerId("buyer-1");
        assertThat(result).hasSize(1);
    }

    // ── createProduct ─────────────────────────────────────────────────────────

    @Test
    void createProduct_savesAndReturnsDto() {
        CreateProductRequest req = CreateProductRequest.builder()
                .title("My Product")
                .description("A description")
                .price(BigDecimal.TEN)
                .category(Category.ELECTRONICS)
                .images(List.of())
                .build();
        Product product = activeProduct("p-new", "author-1");
        ProductDocument doc = productDocument("p-new", "author-1");
        ProductDto dto = ProductDto.builder().id("p-new").build();

        when(minioService.upload(any(List.class))).thenReturn(List.of());
        when(productRepository.save(any())).thenReturn(product);
        when(minioService.buildUrlImage(anyList())).thenReturn(List.of());
        when(productDocumentMapper.mapToDocument(any(), any())).thenReturn(doc);
        when(productSearchRepository.save(any())).thenReturn(doc);
        when(productMapper.mapToDto(any(), any())).thenReturn(dto);

        ProductDto result = productService.createProduct(req, List.of(), "author-1");

        assertThat(result.getId()).isEqualTo("p-new");
        verify(productRepository).save(any());
        verify(productSearchRepository).save(any());
    }

    // ── search ────────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void search_withQuery_returnsResults() {
        ProductDocument doc = productDocument("p-1", "a-1");
        ProductDocumentDto docDto = new ProductDocumentDto();
        docDto.setId("p-1");

        SearchHit<ProductDocument> hit = mock(SearchHit.class);
        SearchHits<ProductDocument> hits = mock(SearchHits.class);

        when(hit.getContent()).thenReturn(doc);
        when(hits.stream()).thenReturn(Stream.of(hit));
        when(elasticsearchTemplate.search(any(Query.class), eq(ProductDocument.class))).thenReturn(hits);
        when(productDocumentMapper.mapDocumentToDto(any())).thenReturn(docDto);

        List<ProductDocumentDto> result = productService.search("laptop", null, null, null);
        assertThat(result).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_withAllFilters_returnsResults() {
        SearchHits<ProductDocument> hits = mock(SearchHits.class);
        when(hits.stream()).thenReturn(Stream.empty());
        when(elasticsearchTemplate.search(any(Query.class), eq(ProductDocument.class))).thenReturn(hits);

        List<ProductDocumentDto> result = productService.search(
                "laptop", Category.ELECTRONICS, BigDecimal.ONE, BigDecimal.valueOf(100));
        assertThat(result).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_noFilters_returnsResults() {
        SearchHits<ProductDocument> hits = mock(SearchHits.class);
        when(hits.stream()).thenReturn(Stream.empty());
        when(elasticsearchTemplate.search(any(Query.class), eq(ProductDocument.class))).thenReturn(hits);

        List<ProductDocumentDto> result = productService.search(null, null, null, null);
        assertThat(result).isEmpty();
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_ownProduct_updatesAndReturnsDto() {
        Product product = activeProduct("p-1", "owner-1");
        product.setImages(new ArrayList<>(List.of("old-img.jpg")));
        ProductDocument doc = productDocument("p-1", "owner-1");
        ProductDto dto = ProductDto.builder().id("p-1").build();

        UpdateProductRequest req = UpdateProductRequest.builder()
                .title("New Title")
                .description("New Description")
                .price(BigDecimal.valueOf(20))
                .category(Category.ELECTRONICS)
                .existingImages(List.of())
                .images(List.of())
                .build();

        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(productSearchRepository.findById("p-1")).thenReturn(Optional.of(doc));
        when(minioService.upload(any(List.class), any(List.class))).thenReturn(List.of("new-img.jpg"));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(productSearchRepository.save(any())).thenReturn(doc);
        when(minioService.buildUrlImage(anyList())).thenReturn(List.of("http://img.jpg"));
        when(productMapper.mapToDto(any(), any())).thenReturn(dto);

        ProductDto result = productService.update("p-1", List.of(), req, "owner-1");

        assertThat(result.getId()).isEqualTo("p-1");
        verify(productRepository).save(any());
        verify(productSearchRepository).save(any());
    }

    @Test
    void update_notOwner_throwsAccessDenied() {
        Product product = activeProduct("p-1", "owner-1");
        ProductDocument doc = productDocument("p-1", "owner-1");

        UpdateProductRequest req = UpdateProductRequest.builder()
                .title("New Title")
                .description("New Description")
                .price(BigDecimal.valueOf(20))
                .category(Category.ELECTRONICS)
                .build();

        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(productSearchRepository.findById("p-1")).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> productService.update("p-1", List.of(), req, "other-user"))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_ownProduct_deletesFromBothRepos() {
        Product product = activeProduct("p-1", "owner-1");
        ProductDocument doc = productDocument("p-1", "owner-1");

        securityUtils.when(SecurityUtils::getCurrentUserIdOrThrow).thenReturn("owner-1");
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(productSearchRepository.findById("p-1")).thenReturn(Optional.of(doc));

        productService.delete("p-1");

        verify(productRepository).delete(product);
        verify(productSearchRepository).delete(doc);
    }

    @Test
    void delete_notOwner_throwsAccessDenied() {
        Product product = activeProduct("p-1", "owner-1");
        ProductDocument doc = productDocument("p-1", "owner-1");

        securityUtils.when(SecurityUtils::getCurrentUserIdOrThrow).thenReturn("other-user");
        when(productRepository.findById("p-1")).thenReturn(Optional.of(product));
        when(productSearchRepository.findById("p-1")).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> productService.delete("p-1"))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ── buy ───────────────────────────────────────────────────────────────────

    @Test
    void buy_validProducts_marksAsSoldAndSaves() {
        Product product = activeProduct("p-1", "seller-1");
        ProductDocument doc = productDocument("p-1", "seller-1");

        when(productRepository.findAllById(List.of("p-1"))).thenReturn(List.of(product));
        when(productSearchRepository.findAllById(List.of("p-1"))).thenReturn(List.of(doc));

        productService.buy(List.of("p-1"), "buyer-1", null);

        assertThat(product.isInStock()).isFalse();
        assertThat(product.getBuyerId()).isEqualTo("buyer-1");
        verify(productRepository).saveAll(List.of(product));
        verify(productSearchRepository).saveAll(List.of(doc));
    }

    @Test
    void buy_withPromoCode_savesPromoCodeUsage() {
        Product product = activeProduct("p-1", "seller-1");
        ProductDocument doc = productDocument("p-1", "seller-1");

        when(productRepository.findAllById(List.of("p-1"))).thenReturn(List.of(product));
        when(productSearchRepository.findAllById(List.of("p-1"))).thenReturn(List.of(doc));
        when(promoCodeUsageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        productService.buy(List.of("p-1"), "buyer-1", "promo-1");

        verify(promoCodeUsageRepository).save(any(PromoCodeUsage.class));
    }

    @Test
    void buy_productOutOfStock_skips() {
        Product product = activeProduct("p-1", "seller-1");
        product.setInStock(false);

        when(productRepository.findAllById(List.of("p-1"))).thenReturn(List.of(product));
        when(productSearchRepository.findAllById(List.of("p-1"))).thenReturn(List.of());

        productService.buy(List.of("p-1"), "buyer-1", null);

        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void buy_buyerIsAuthor_skips() {
        Product product = activeProduct("p-1", "buyer-1");

        when(productRepository.findAllById(List.of("p-1"))).thenReturn(List.of(product));
        when(productSearchRepository.findAllById(List.of("p-1"))).thenReturn(List.of());

        productService.buy(List.of("p-1"), "buyer-1", null);

        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void buy_bannedProduct_throwsForbidden() {
        Product product = activeProduct("p-1", "seller-1");
        product.setProductStatus(ProductStatus.BANNED);

        when(productRepository.findAllById(List.of("p-1"))).thenReturn(List.of(product));
        when(productSearchRepository.findAllById(List.of("p-1"))).thenReturn(List.of());

        assertThatThrownBy(() -> productService.buy(List.of("p-1"), "buyer-1", null))
                .isInstanceOf(ForbiddenException.class);
    }
}
