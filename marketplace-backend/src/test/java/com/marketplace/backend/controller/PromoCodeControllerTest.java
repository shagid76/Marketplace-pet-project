package com.marketplace.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.PromoCodeDto;
import com.marketplace.backend.model.PromoCode.CheckPromoCodeRequest;
import com.marketplace.backend.model.PromoCode.CreatePromoCodeRequest;
import com.marketplace.backend.model.PromoCode.PromoCodeType;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.PromoCodeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.marketplace.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PromoCodeController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class PromoCodeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean
    JwtService jwtService;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean PromoCodeService promoCodeService;

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

    private PromoCodeDto sampleDto() {
        PromoCodeDto dto = new PromoCodeDto();
        dto.setId("promo-1");
        dto.setCode("SAVE10");
        return dto;
    }

//    @Test
//    @WithMockUser(roles = "MODERATOR")
//    void create_returnsCreated() throws Exception {
//        CreatePromoCodeRequest req = new CreatePromoCodeRequest();
//        req.setCode("SAVE10");
//        req.setPromoCodeType(PromoCodeType.PERCENTAGE);
//        req.setDiscountValue(10.0);
//
//        when(promoCodeService.create(any())).thenReturn(sampleDto());
//
//        mockMvc.perform(post("/api/promo_codes")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value("promo-1"));
//    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_returnsPage() throws Exception {
        when(promoCodeService.findAll(0, 5)).thenReturn(new PageResponseDto<>());

        mockMvc.perform(get("/api/promo_codes").param("page", "0").param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getByCode_returnsDto() throws Exception {
        when(promoCodeService.findByCode("SAVE10")).thenReturn(sampleDto());

        mockMvc.perform(get("/api/promo_codes/SAVE10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE10"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivate_returnsUpdatedDto() throws Exception {
        PromoCodeDto deactivated = sampleDto();
        deactivated.setActive(false);

        when(promoCodeService.deactivate("promo-1")).thenReturn(deactivated);

        mockMvc.perform(patch("/api/promo_codes/promo-1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser
    void checkPromoCode_returnsDto() throws Exception {
        CheckPromoCodeRequest req = new CheckPromoCodeRequest();
        req.setCode("SAVE10");
        req.setProductIds(List.of("p-1"));

        when(promoCodeService.checkPromoCode(any(), eq("user-1"))).thenReturn(sampleDto());

        mockMvc.perform(post("/api/promo_codes/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("promo-1"));
    }
}
