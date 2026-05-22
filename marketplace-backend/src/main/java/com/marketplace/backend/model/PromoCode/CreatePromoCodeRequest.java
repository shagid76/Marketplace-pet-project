package com.marketplace.backend.model.PromoCode;

import com.marketplace.backend.model.Product.Category;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePromoCodeRequest {

    @NotBlank(message = "Promo code is required")
    @Size(min = 3, max = 32, message = "Promo code must be between 3 and 32 characters")
    private String code;

    @NotNull(message = "Promo code type is required")
    private PromoCodeType promoCodeType;

    @NotNull(message = "Start date is required")
    private LocalDateTime startAt;

    @NotNull(message = "End date is required")
    private LocalDateTime endAt;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be greater than zero")
    private Double discountValue;

    @NotNull(message = "Max usage per user is required")
    @Min(value = 1, message = "Max usage per user must be at least 1")
    private Integer maxUsagePerUser;

    @NotEmpty(message = "At least one applicable category is required")
    private List<Category> applicableCategories;

    @NotNull(message = "Required products count is required")
    @Min(value = 1, message = "Required products must be at least 1")
    private Integer requiredProducts;

    @NotNull(message = "Required minimum price is required")
    @DecimalMin(value = "0.0", message = "Required price must be 0 or greater")
    private BigDecimal requiredPrice;
}
