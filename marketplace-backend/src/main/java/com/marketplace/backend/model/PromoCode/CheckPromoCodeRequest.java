package com.marketplace.backend.model.PromoCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckPromoCodeRequest {

    @NotBlank(message = "Promo code is required")
    private String code;

    @NotEmpty(message = "At least one product ID is required")
    private List<String> productIds;
}
