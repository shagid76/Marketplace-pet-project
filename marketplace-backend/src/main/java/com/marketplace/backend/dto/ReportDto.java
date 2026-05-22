package com.marketplace.backend.dto;

import com.marketplace.backend.model.Report.Status;
import com.marketplace.backend.model.Report.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDto {
    private String id;
    private String authorId;
    private String targetId;
    private String description;
    private Status status;
    private TargetType targetType;
}