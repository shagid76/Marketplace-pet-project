package com.marketplace.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.ReportDto;
import com.marketplace.backend.model.Report.CreateReportRequest;
import com.marketplace.backend.model.Report.TargetType;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.ReportService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
class ReportControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ReportService reportService;

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

    private ReportDto sampleDto() {
        ReportDto dto = new ReportDto();
        dto.setId("report-1");
        return dto;
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void findAll_returnsPage() throws Exception {
        when(reportService.findAll(0, 5)).thenReturn(new PageResponseDto<>());

        mockMvc.perform(get("/api/reports/active").param("page", "0").param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void create_validRequest_returnsCreated() throws Exception {
        CreateReportRequest req = CreateReportRequest.builder()
                .targetId("user-2")
                .description("This user is spamming everywhere")
                .targetType(TargetType.USER)
                .build();

        when(reportService.create(any(), eq("user-1"))).thenReturn(sampleDto());

        mockMvc.perform(post("/api/reports/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("report-1"));
    }

    @Test
    @WithMockUser
    void create_shortDescription_returns400() throws Exception {
        CreateReportRequest req = CreateReportRequest.builder()
                .targetId("user-2")
                .description("short")
                .targetType(TargetType.USER)
                .build();

        mockMvc.perform(post("/api/reports/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void solve_returnsUpdatedDto() throws Exception {
        when(reportService.solve("report-1")).thenReturn(sampleDto());

        mockMvc.perform(patch("/api/reports/solve/report-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("report-1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/reports/report-1"))
                .andExpect(status().isNoContent());

        verify(reportService).delete("report-1");
    }
}
