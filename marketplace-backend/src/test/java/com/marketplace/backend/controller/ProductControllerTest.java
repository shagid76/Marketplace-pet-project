package com.marketplace.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.ProductDocumentDto;
import com.marketplace.backend.dto.ProductDto;
import com.marketplace.backend.model.Product.Category;
import com.marketplace.backend.model.Product.CreateProductRequest;
import com.marketplace.backend.model.Product.UpdateProductRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.marketplace.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ProductService productService;

    private MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void setUp() {
        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUserIdOrThrow).thenReturn("user-1");
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    private ProductDto productDto(String id) {
        ProductDto dto = new ProductDto();
        dto.setId(id);
        return dto;
    }

    @Test
    @WithMockUser
    void getProduct_returnsDto() throws Exception {
        when(productService.findByProductId("p-1")).thenReturn(productDto("p-1"));

        mockMvc.perform(get("/api/products/p-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-1"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void findAll_returnsPage() throws Exception {
        when(productService.findAll(0, 5)).thenReturn(new PageResponseDto<>());

        mockMvc.perform(get("/api/products").param("page", "0").param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void categories_returnsAllCategories() throws Exception {
        when(productService.findAllCategories()).thenReturn(List.of(Category.ELECTRONICS, Category.BABY));

        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("ELECTRONICS"));
    }

    @Test
    @WithMockUser
    void getAllProductsByTargetId_returnsList() throws Exception {
        when(productService.findAllByAuthorId("author-1")).thenReturn(List.of(productDto("p-1")));

        mockMvc.perform(get("/api/products/author").param("targetId", "author-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p-1"));
    }

    @Test
    @WithMockUser
    void getAllMyProducts_delegatesToService() throws Exception {
        when(productService.findAllByAuthorId("user-1")).thenReturn(List.of(productDto("p-1")));

        mockMvc.perform(get("/api/products/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p-1"));
    }

    @Test
    @WithMockUser
    void getAllMyPurchases_delegatesToService() throws Exception {
        when(productService.findAllByBuyerId("user-1")).thenReturn(List.of(productDto("p-2")));

        mockMvc.perform(get("/api/products/me/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p-2"));
    }

    @Test
    @WithMockUser
    void getNewProducts_returnsTopItems() throws Exception {
        when(productService.findNewItems(10)).thenReturn(List.of(productDto("p-1")));

        mockMvc.perform(get("/api/products/new-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p-1"));
    }

    @Test
    @WithMockUser
    void getProductsByCategory_returnsFiltered() throws Exception {
        when(productService.findAllByCategory(Category.ELECTRONICS)).thenReturn(List.of(productDto("p-1")));

        mockMvc.perform(get("/api/products/category/ELECTRONICS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p-1"));
    }

    @Test
    @WithMockUser
    void search_withParams_returnsResults() throws Exception {
        ProductDocumentDto docDto = new ProductDocumentDto();
        docDto.setId("p-1");

        when(productService.search(eq("laptop"), eq(Category.ELECTRONICS), any(), any()))
                .thenReturn(List.of(docDto));

        mockMvc.perform(get("/api/products/search")
                        .param("query", "laptop")
                        .param("category", "ELECTRONICS")
                        .param("minPrice", "10")
                        .param("maxPrice", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p-1"));
    }

    @Test
    @WithMockUser
    void search_noParams_returnsResults() throws Exception {
        when(productService.search(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/products/search"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void createProduct_multipart_returnsCreated() throws Exception {
        MockMultipartFile productPart = new MockMultipartFile(
                "product", "product.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(
                        CreateProductRequest.builder()
                                .title("My Product")
                                .description("Long enough description here")
                                .price(BigDecimal.TEN)
                                .category(Category.ELECTRONICS)
                                .build()
                )
        );
        MockMultipartFile imagePart = new MockMultipartFile(
                "images", "img.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        when(productService.createProduct(any(), any(), eq("user-1"))).thenReturn(productDto("p-new"));

        mockMvc.perform(multipart("/api/products")
                        .file(productPart)
                        .file(imagePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("p-new"));
    }

    @Test
    @WithMockUser
    void updateProduct_multipart_returnsOk() throws Exception {
        MockMultipartFile productPart = new MockMultipartFile(
                "product", "product.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(
                        UpdateProductRequest.builder()
                                .title("Updated Title")
                                .description("Updated description here")
                                .price(BigDecimal.valueOf(20))
                                .category(Category.ELECTRONICS)
                                .build()
                )
        );

        when(productService.update(eq("p-1"), any(), any(), eq("user-1"))).thenReturn(productDto("p-1"));

        mockMvc.perform(multipart("/api/products/p-1")
                        .file(productPart)
                        .with(request -> { request.setMethod("PATCH"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-1"));
    }

    @Test
    @WithMockUser
    void deleteProduct_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/p-1"))
                .andExpect(status().isNoContent());

        verify(productService).delete("p-1");
    }
}
