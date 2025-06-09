package com.marketplace.marketplace.service;

import com.marketplace.marketplace.model.Report.CreateReportRequest;
import com.marketplace.marketplace.model.Report.Report;
import com.marketplace.marketplace.repository.ReportRepository;
import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    public Report findById(String id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report with id + " + id + " not found"));
    }

    public Report createReport(CreateReportRequest createReportRequest){
        Report report = new Report();
        report.setId(UUID.randomUUID().toString());
        report.setTitle(createReportRequest.getTitle());
        report.setDescription(createReportRequest.getDescription());
        report.setReporterId(createReportRequest.getReporterId());
        report.setViolatorId(createReportRequest.getViolatorId());
        report.setItemId(createReportRequest.getItemId());
        return  reportRepository.save(report);
    }

    public void resolveReport(String id){
        reportRepository.delete(findById(id));
    }
}