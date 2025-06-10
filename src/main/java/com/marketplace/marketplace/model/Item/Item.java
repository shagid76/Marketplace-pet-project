package com.marketplace.marketplace.model.Item;

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
@Document(collection = "items")
public class Item {
    @Id
    private String id;
    private String name;
    private float price;
    private LocalDate listedDate;
    private boolean isService;
    private boolean isSolded;
    private boolean isBanned;
    private ItemCategory category;
}