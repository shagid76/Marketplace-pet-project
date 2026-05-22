package com.marketplace.backend.service;

import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.ReportDto;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.PageMapper;
import com.marketplace.backend.mapper.ReportMapper;
import com.marketplace.backend.model.Report.CreateReportRequest;
import com.marketplace.backend.model.Report.Report;
import com.marketplace.backend.model.Report.Status;
import com.marketplace.backend.repository.ReportRepository;
import com.marketplace.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportMapper reportMapper;
    private final PageMapper pageMapper;

    private Page<ReportDto> findPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reportRepository.findAllByStatus(Status.ACTIVE, pageable)
                .map(reportMapper::mapToDto);
    }

    public PageResponseDto<ReportDto> findAll(int page, int size) {
        return pageMapper.mapToDto(page, size, findPage(page, size));
    }

    private Report findById(String id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found"));
    }

    public ReportDto create(CreateReportRequest createReportRequest, String authorId) {
        if (!userRepository.existsById(authorId)) {
            throw new NotFoundException("User not found");
        }
        Report report = createReport(createReportRequest, authorId);
        return reportMapper.mapToDto(report);
    }

    private Report createReport(CreateReportRequest createReportRequest, String authorId) {
        Report report = Report.builder()
                .id(UUID.randomUUID().toString())
                .authorId(authorId)
                .targetId(createReportRequest.getTargetId())
                .description(createReportRequest.getDescription())
                .status(Status.ACTIVE)
                .targetType(createReportRequest.getTargetType())
                .build();
        return reportRepository.save(report);
    }

    public ReportDto solve(String reportId) {
        Report report = findById(reportId);
        report.setStatus(Status.ARCHIVED);
        reportRepository.save(report);
        return reportMapper.mapToDto(report);
    }

    public void delete(String reportId) {
        Report report = findById(reportId);
        reportRepository.delete(report);
    }
}