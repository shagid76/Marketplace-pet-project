package com.marketplace.backend.dto;

import com.marketplace.backend.model.Admin.ActionType;
import com.marketplace.backend.model.Admin.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminActionDto {
    private String id;
    private String actorId;
    private ActionType actionType;
    private TargetType targetType;
    private String targetId;
    private String reason;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokedBy;
    private String revokeReason;
    private boolean active;
}