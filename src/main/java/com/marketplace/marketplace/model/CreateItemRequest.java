package com.marketplace.marketplace.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateItemRequest {
    @NotBlank(message = "Name can't be empty!")
    @Size(min = 4, max = 64, message = "Item name must be between 4 and 64 characters!")
    private String name;
    @NotBlank(message = "Price can't be empty!")
    @DecimalMin(value = "0.01", message = "Price must be greater than or equal to 0.01")
    private float price;
    private LocalDate listedDate;
    private boolean isService;
    @NotBlank(message = "Category can't be empty!")
    private ItemCategory category;
}