package com.marketplace.backend.model.Admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAdminActionRequest {

    @NotNull(message = "Action type is required")
    private ActionType actionType;

    @NotNull(message = "Target type is required")
    private TargetType targetType;

    @NotBlank(message = "Target ID is required")
    private String targetId;

    @NotBlank(message = "Reason is required")
    @Size(min = 3, max = 512, message = "Reason must be between 3 and 512 characters")
    private String reason;

    private Instant expiresAt;
}
