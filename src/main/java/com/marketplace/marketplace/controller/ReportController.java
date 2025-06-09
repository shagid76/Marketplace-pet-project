package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.model.Report.CreateReportRequest;
import com.marketplace.marketplace.model.Report.Report;
import com.marketplace.marketplace.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    public List<Report> findAll(){
        return reportService.findAll();
    }

    @PostMapping("/create-report")
    public Report createReport(@RequestParam String reporterId, @RequestParam String violatorId,
                          @RequestParam(required = false) String itemId) {
        CreateReportRequest createReportRequest = new CreateReportRequest();
        createReportRequest.setReporterId(reporterId);
        createReportRequest.setViolatorId(violatorId);
        createReportRequest.setItemId(itemId);
        return reportService.createReport(createReportRequest);
    }

    @DeleteMapping("/resolve")
    public void resolve(@RequestParam String id){
        reportService.resolveReport(id);
    }
}