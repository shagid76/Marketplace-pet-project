package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.ReportDto;
import com.marketplace.backend.model.Report.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportMapper {
    public ReportDto mapToDto(Report report) {
        return ReportDto.builder()
                .id(report.getId())
                .authorId(report.getAuthorId())
                .targetId(report.getTargetId())
                .description(report.getDescription())
                .status(report.getStatus())
                .targetType(report.getTargetType())
                .build();
    }
}