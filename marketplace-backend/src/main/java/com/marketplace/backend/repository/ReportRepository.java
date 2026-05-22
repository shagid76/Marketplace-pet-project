package com.marketplace.backend.repository;

import com.marketplace.backend.model.Report.Report;
import com.marketplace.backend.model.Report.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends MongoRepository<Report, String> {
    Page<Report> findAllByStatus(Status status, Pageable pageable);
}
