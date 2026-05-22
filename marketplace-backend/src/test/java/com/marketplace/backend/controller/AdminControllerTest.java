package com.marketplace.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.backend.dto.AdminActionDto;
import com.marketplace.backend.model.Admin.*;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.security.jwt.JwtService;
import com.marketplace.backend.service.AdminService;
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

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean AdminService adminService;
    @MockitoBean JwtService jwtService;

    private MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void setUp() {
        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUserIdOrThrow).thenReturn("mod-1");
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    private AdminActionDto sampleDto() {
        AdminActionDto dto = new AdminActionDto();
        dto.setId("action-1");
        return dto;
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void create_validRequest_returns200() throws Exception {
        CreateAdminActionRequest req = new CreateAdminActionRequest();
        req.setTargetId("user-1");
        req.setTargetType(TargetType.USER);
        req.setActionType(ActionType.BAN);
        req.setReason("Spammer");

        when(adminService.create(any(), eq("mod-1"))).thenReturn(sampleDto());

        mockMvc.perform(post("/api/admin-actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("action-1"));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getActive_returns200() throws Exception {
        when(adminService.getActive(eq("user-1"), eq(TargetType.USER))).thenReturn(sampleDto());

        mockMvc.perform(get("/api/admin-actions/active")
                        .param("targetId", "user-1")
                        .param("targetType", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("action-1"));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void revoke_returns200() throws Exception {
        mockMvc.perform(delete("/api/admin-actions/revoke/action-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void extend_validRequest_returns200() throws Exception {
        ExtendAdminActionRequest req = new ExtendAdminActionRequest();
        req.setNewExpiresAt(Instant.now().plusSeconds(3600));
        req.setReason("extending block");

        when(adminService.extend(any(), eq("action-1"))).thenReturn(sampleDto());

        mockMvc.perform(patch("/api/admin-actions/extend/action-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("action-1"));
    }
}
