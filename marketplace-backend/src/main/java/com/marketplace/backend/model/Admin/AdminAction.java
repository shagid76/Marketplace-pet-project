package com.marketplace.backend.model.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "admin_actions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminAction {
    @Id
    private String id;

    private String actorId;
    private ActionType actionType;
    private TargetType targetType;
    private String targetId;

    private String reason;
    private Instant createdAt;
    private Instant expiresAt;
    private String revokedBy;
    private Instant revokedAt;
    private String revokeReason;
}