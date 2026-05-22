package com.marketplace.backend.controller;

import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Real-time Events", description = "Server-Sent Events for live moderation notifications")
@SecurityRequirement(name = "bearerAuth")
public class SseController {
    private final SseEmitterService sseEmitterService;

    @Operation(summary = "Open an SSE stream to receive live notifications")
    @GetMapping("/subscribe")
    @PreAuthorize("isAuthenticated()")
    public SseEmitter subscribe() {
        String userId = SecurityUtils.getCurrentUserIdOrThrow();
        return sseEmitterService.subscribe(userId);
    }
}