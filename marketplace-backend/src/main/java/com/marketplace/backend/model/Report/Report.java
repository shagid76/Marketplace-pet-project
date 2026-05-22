package com.marketplace.backend.model.Report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reports")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    private String id;
    private String authorId;
    private String targetId;
    private String description;
    private Status status;
    private TargetType targetType;
}