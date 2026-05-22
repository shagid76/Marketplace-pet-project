package com.marketplace.backend.controller;

import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.SseEmitterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import com.marketplace.backend.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SseController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class SseControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean SseEmitterService sseEmitterService;

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

    @Test
    @WithMockUser
    void subscribe_returnsEmitter() throws Exception {
        when(sseEmitterService.subscribe("user-1")).thenReturn(new SseEmitter());

        mockMvc.perform(get("/api/events/subscribe"))
                .andExpect(status().isOk());

        verify(sseEmitterService).subscribe("user-1");
    }
}
