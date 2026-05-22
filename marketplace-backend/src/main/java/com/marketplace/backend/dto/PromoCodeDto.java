package com.marketplace.backend.dto;

import com.marketplace.backend.model.Product.Category;
import com.marketplace.backend.model.PromoCode.PromoCodeType;
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
public class PromoCodeDto {
    private String id;
    private String code;
    private String stripeCouponId;
    private PromoCodeType promoCodeType;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Double discountValue;
    private Integer maxUsagePerUser;
    private List<Category> applicableCategories;
    private Integer requiredProducts;
    private BigDecimal requiredPrice;
    private boolean active;
}
