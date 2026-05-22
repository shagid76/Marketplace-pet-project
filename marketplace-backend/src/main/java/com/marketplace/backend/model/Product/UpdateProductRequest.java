package com.marketplace.backend.model.Product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 128, message = "Title must be between 5 and 128 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 256, message = "Description must be between 10 and 256 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    private List<MultipartFile> images = new ArrayList<>();
    private List<String> existingImages = new ArrayList<>();

    @NotNull(message = "Category is required")
    private Category category;
}