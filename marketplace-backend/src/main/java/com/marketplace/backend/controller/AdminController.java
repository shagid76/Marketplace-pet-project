package com.marketplace.backend.controller;

import com.marketplace.backend.dto.AdminActionDto;
import com.marketplace.backend.model.Admin.CreateAdminActionRequest;
import com.marketplace.backend.model.Admin.ExtendAdminActionRequest;
import com.marketplace.backend.model.Admin.TargetType;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin-actions")
@Tag(name = "Admin Actions", description = "Bans, warnings, and moderation actions on users and products")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final AdminService adminService;

    @Operation(summary = "Apply a moderation action (ban/warn) to a user or product")
    @PostMapping()
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public AdminActionDto create(@RequestBody @Valid CreateAdminActionRequest createAdminActionRequest) {
        return adminService.create(createAdminActionRequest, SecurityUtils.getCurrentUserIdOrThrow());
    }

    @PreAuthorize("hasAnyRole('ROLE_MODERATOR','ROLE_ADMIN')")
    @Operation(summary = "Get the active moderation action for a target")
    @GetMapping("/active")
    public AdminActionDto getActive(@RequestParam String targetId, @RequestParam TargetType targetType) {
        return adminService.getActive(targetId, targetType);
    }

    @Operation(summary = "Revoke an active moderation action")
    @DeleteMapping("/revoke/{adminActionId}")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public void revoke(@PathVariable String adminActionId) {
        adminService.revoke(adminActionId);
    }

    @Operation(summary = "Extend the duration of an active moderation action")
    @PatchMapping("/extend/{adminActionId}")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public AdminActionDto extend(@RequestBody @Valid ExtendAdminActionRequest extendAdminActionRequest,
                                 @PathVariable String adminActionId) {
        return adminService.extend(extendAdminActionRequest, adminActionId);
    }
}
