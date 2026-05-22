package com.marketplace.backend.search;

import com.marketplace.backend.AbstractElasticsearchIntegrationTest;
import com.marketplace.backend.dto.ProductDocumentDto;
import com.marketplace.backend.model.Product.Category;
import com.marketplace.backend.model.Product.ProductDocument;
import com.marketplace.backend.repository.ProductSearchRepository;
import com.marketplace.backend.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Elasticsearch-backed product search logic.
 *
 * A real ES node is started via {@link AbstractElasticsearchIntegrationTest}.
 * Each test class clears the index before/after to stay independent.
 */
@DisplayName("Product Search – Elasticsearch integration")
class ProductSearchIntegrationTest extends AbstractElasticsearchIntegrationTest {

    @Autowired
    private ProductSearchRepository searchRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ProductService productService;

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private ProductDocument laptop;
    private ProductDocument phone;
    private ProductDocument jacket;
    private ProductDocument bannedLaptop;
    private ProductDocument lockedPhone;
    private ProductDocument outOfStockCamera;

    @BeforeEach
    void seedIndex() {
        laptop = doc("Gaming Laptop Pro", "High-end gaming laptop", Category.ELECTRONICS,
                new BigDecimal("1299.99"), true, false, false);
        phone = doc("Smartphone X12", "Latest flagship phone", Category.ELECTRONICS,
                new BigDecimal("799.00"), true, false, false);
        jacket = doc("Winter Jacket", "Warm down jacket", Category.FASHION,
                new BigDecimal("149.99"), true, false, false);
        bannedLaptop = doc("Cheap Laptop", "Refurbished laptop", Category.ELECTRONICS,
                new BigDecimal("399.00"), true, false, true);   // banned
        lockedPhone = doc("Used Phone", "Old phone", Category.ELECTRONICS,
                new BigDecimal("299.00"), true, true, false);   // locked
        outOfStockCamera = doc("DSLR Camera", "Professional camera", Category.ELECTRONICS,
                new BigDecimal("899.00"), false, false, false); // not in stock

        searchRepository.saveAll(List.of(laptop, phone, jacket, bannedLaptop, lockedPhone, outOfStockCamera));

        // Give ES a moment to make documents searchable (index refresh)
        elasticsearchOperations.indexOps(ProductDocument.class).refresh();
    }

    @AfterEach
    void clearIndex() {
        searchRepository.deleteAll();
        elasticsearchOperations.indexOps(ProductDocument.class).refresh();
    }

    // -------------------------------------------------------------------------
    // Visibility filter
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Visibility filter")
    class VisibilityFilter {

        @Test
        @DisplayName("excludes banned products from all results")
        void excludesBanned() {
            List<ProductDocumentDto> results = productService.search(null, null, null, null);

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .doesNotContain(bannedLaptop.getId());
        }

        @Test
        @DisplayName("excludes locked products from all results")
        void excludesLocked() {
            List<ProductDocumentDto> results = productService.search(null, null, null, null);

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .doesNotContain(lockedPhone.getId());
        }

        @Test
        @DisplayName("excludes out-of-stock products from all results")
        void excludesOutOfStock() {
            List<ProductDocumentDto> results = productService.search(null, null, null, null);

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .doesNotContain(outOfStockCamera.getId());
        }

        @Test
        @DisplayName("returns only inStock=true, banned=false, locked=false documents")
        void returnsOnlyVisibleProducts() {
            List<ProductDocumentDto> results = productService.search(null, null, null, null);

            assertThat(results)
                    .extracting(ProductDocumentDto::getId)
                    .containsExactlyInAnyOrder(laptop.getId(), phone.getId(), jacket.getId());
        }
    }


    @Nested
    @DisplayName("Text query")
    class TextQuery {

        @Test
        @DisplayName("finds products whose title contains the query term")
        void matchesByTitle() {
            List<ProductDocumentDto> results = productService.search("laptop", null, null, null);

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .containsExactly(laptop.getId());
        }

        @Test
        @DisplayName("is case-insensitive")
        void caseInsensitive() {
            List<ProductDocumentDto> results = productService.search("LAPTOP", null, null, null);

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .containsExactly(laptop.getId());
        }

        @Test
        @DisplayName("returns empty list when no product title matches")
        void noMatch() {
            List<ProductDocumentDto> results = productService.search("zzznonexistent", null, null, null);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("null query returns all visible products")
        void nullQueryReturnsAll() {
            List<ProductDocumentDto> results = productService.search(null, null, null, null);

            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("empty string query returns all visible products")
        void emptyQueryReturnsAll() {
            List<ProductDocumentDto> results = productService.search("", null, null, null);

            assertThat(results).hasSize(3);
        }
    }

    // -------------------------------------------------------------------------
    // Category filter
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Category filter")
    class CategoryFilter {

        @Test
        @DisplayName("returns only products matching the given category")
        void filterByCategory() {
            List<ProductDocumentDto> results = productService.search(null, Category.FASHION, null, null);

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .containsExactly(jacket.getId());
        }

        @Test
        @DisplayName("ELECTRONICS category returns only visible electronics")
        void electronicsCategory() {
            List<ProductDocumentDto> results = productService.search(null, Category.ELECTRONICS, null, null);

            // laptop and phone are visible; bannedLaptop, lockedPhone, outOfStockCamera are filtered out
            assertThat(results).extracting(ProductDocumentDto::getId)
                    .containsExactlyInAnyOrder(laptop.getId(), phone.getId());
        }

        @Test
        @DisplayName("returns empty list for category with no visible products")
        void emptyCategory() {
            List<ProductDocumentDto> results = productService.search(null, Category.MOTORS, null, null);

            assertThat(results).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // Price range filter
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Price range filter")
    class PriceRange {

        @Test
        @DisplayName("minPrice filter excludes cheaper products")
        void minPrice() {
            // only laptop (1299.99) is above 800
            List<ProductDocumentDto> results = productService.search(null, null, new BigDecimal("800.00"), null);

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .containsExactly(laptop.getId());
        }

        @Test
        @DisplayName("maxPrice filter excludes more expensive products")
        void maxPrice() {
            // jacket (149.99) is the only one at or below 200
            List<ProductDocumentDto> results = productService.search(null, null, null, new BigDecimal("200.00"));

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .containsExactly(jacket.getId());
        }

        @Test
        @DisplayName("min and max price together define an inclusive range")
        void priceRange() {
            // phone (799.00) falls in [700, 850], laptop (1299.99) does not
            List<ProductDocumentDto> results = productService.search(
                    null, null, new BigDecimal("700.00"), new BigDecimal("850.00"));

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .containsExactly(phone.getId());
        }

        @Test
        @DisplayName("exact boundary prices are included (greaterThanEqual / lessThanEqual)")
        void exactBoundary() {
            List<ProductDocumentDto> results = productService.search(
                    null, null, new BigDecimal("799.00"), new BigDecimal("799.00"));

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .containsExactly(phone.getId());
        }
    }

    // -------------------------------------------------------------------------
    // Combined filters
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Combined filters")
    class CombinedFilters {

        @Test
        @DisplayName("query + category narrows results correctly")
        void queryAndCategory() {
            List<ProductDocumentDto> results = productService.search("laptop", Category.ELECTRONICS, null, null);

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .containsExactly(laptop.getId());
        }

        @Test
        @DisplayName("query + price range filters simultaneously")
        void queryAndPriceRange() {
            // 'phone' in title, price <= 900 → Smartphone X12
            List<ProductDocumentDto> results = productService.search(
                    "phone", null, null, new BigDecimal("900.00"));

            assertThat(results).extracting(ProductDocumentDto::getId)
                    .containsExactly(phone.getId());
        }

        @Test
        @DisplayName("query + category + price returns empty when no document matches all criteria")
        void noMatchAllCriteria() {
            // FASHION with price > 1000 — jacket is only 149.99
            List<ProductDocumentDto> results = productService.search(
                    null, Category.FASHION, new BigDecimal("1000.00"), null);

            assertThat(results).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ProductDocument doc(String title, String description, Category category,
                                BigDecimal price, boolean inStock, boolean locked, boolean banned) {
        return ProductDocument.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .category(category)
                .price(price)
                .inStock(inStock)
                .locked(locked)
                .banned(banned)
                .author(UUID.randomUUID().toString())
                .images(List.of())
                .build();
    }
}
