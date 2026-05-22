package com.marketplace.backend.controller;

import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.ReportDto;
import com.marketplace.backend.model.Report.CreateReportRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "User and product abuse reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    private final ReportService reportService;

    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN')")
    @Operation(summary = "List all active reports (admin/moderator only)")
    @GetMapping("/active")
    public ResponseEntity<PageResponseDto<ReportDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "5") int size) {
        PageResponseDto<ReportDto> reports = reportService.findAll(page, size);
        return ResponseEntity.ok(reports);
    }

    @Operation(summary = "Submit a new report against a user or product")
    @PostMapping("/create")
    public ResponseEntity<ReportDto> create(@RequestBody @Valid CreateReportRequest createReportRequest) {
        ReportDto report = reportService.create(createReportRequest, SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @Operation(summary = "Mark a report as resolved (admin/moderator only)")
    @PatchMapping("/solve/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<ReportDto> solve(@PathVariable String id) {
        ReportDto report = reportService.solve(id);
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Delete a report (admin/moderator only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}