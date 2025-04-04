package com.marketplace.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document
public class Item {
    @Id
    private String id;
    private String name;
    private float value;
    private boolean isFree;
    private LocalDate listedDate;
    private boolean isService;
    private boolean isSolded;
    private ItemCategory category;
}