package com.marketplace.marketplace.repository;

import com.marketplace.marketplace.model.Report.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, String> {
}
