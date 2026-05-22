package com.marketplace.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.ReviewDto;
import com.marketplace.backend.model.Review.CreateReviewRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.ReviewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.marketplace.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReviewController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class ReviewControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ReviewService reviewService;

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

    private ReviewDto sampleDto() {
        ReviewDto dto = new ReviewDto();
        dto.setId("review-1");
        return dto;
    }

    @Test
    @WithMockUser
    void createReview_validRequest_returnsCreated() throws Exception {
        CreateReviewRequest req = CreateReviewRequest.builder()
                .description("Great seller, fast shipping!")
                .rating(4.5)
                .targetId("seller-1")
                .build();

        when(reviewService.createOrUpdateReview(any(), eq("user-1"))).thenReturn(sampleDto());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("review-1"));
    }

    @Test
    @WithMockUser
    void createReview_shortDescription_returns400() throws Exception {
        CreateReviewRequest req = CreateReviewRequest.builder()
                .description("Bad")
                .rating(1.0)
                .targetId("seller-1")
                .build();

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReviews_returnsPage() throws Exception {
        when(reviewService.findAll(0, 5)).thenReturn(new PageResponseDto<>());

        mockMvc.perform(get("/api/reviews").param("page", "0").param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void findAverageRating_returnsValue() throws Exception {
        when(reviewService.findAverageRating("seller-1")).thenReturn(4.2);

        mockMvc.perform(get("/api/reviews/average/seller-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4.2));
    }

    @Test
    @WithMockUser
    void findMyAverageRating_returnsValue() throws Exception {
        when(reviewService.findAverageRating("user-1")).thenReturn(3.8);

        mockMvc.perform(get("/api/reviews/average/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3.8));
    }

    @Test
    @WithMockUser
    void findReviewsByTarget_returnsList() throws Exception {
        when(reviewService.findAllReviewsByTargetId("seller-1")).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get("/api/reviews/seller-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("review-1"));
    }

    @Test
    @WithMockUser
    void findMyReview_returnsDto() throws Exception {
        when(reviewService.findReviewByAuthorAndTargetId("user-1", "seller-1")).thenReturn(sampleDto());

        mockMvc.perform(get("/api/reviews/my-review").param("targetId", "seller-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("review-1"));
    }

    @Test
    @WithMockUser
    void deleteReview_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/reviews/review-1"))
                .andExpect(status().isNoContent());

        verify(reviewService).delete("review-1", "user-1");
    }
}
