package com.marketplace.backend.model.Report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReportRequest {

    @NotBlank(message = "Target ID is required")
    private String targetId;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 256, message = "Description must be between 10 and 256 characters")
    private String description;

    @NotNull(message = "Target type is required")
    private TargetType targetType;
}