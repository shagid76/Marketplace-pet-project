package com.marketplace.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.backend.model.Payment.CreatePaymentRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.security.jwt.JwtService;
import com.marketplace.backend.service.PaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class PaymentControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean PaymentService paymentService;

    private MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void setUp() {
        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUserIdOrThrow).thenReturn("buyer-1");
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    @WithMockUser
    void createPayment_returnsCheckoutUrl() throws Exception {
        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .productId(List.of("p-1")).build();

        when(paymentService.createStripeSessionId(any(), eq("buyer-1"), isNull()))
                .thenReturn("https://checkout.stripe.com/session-id");

        mockMvc.perform(post("/api/payments/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().string("https://checkout.stripe.com/session-id"));
    }

    @Test
    @WithMockUser
    void createPayment_withPromoCode_passesPromoToService() throws Exception {
        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .productId(List.of("p-1")).build();

        when(paymentService.createStripeSessionId(any(), eq("buyer-1"), eq("SAVE10")))
                .thenReturn("https://checkout.stripe.com/promo-session");

        mockMvc.perform(post("/api/payments/create")
                        .param("promoCode", "SAVE10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().string("https://checkout.stripe.com/promo-session"));
    }
}
