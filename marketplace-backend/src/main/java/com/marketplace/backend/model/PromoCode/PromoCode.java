package com.marketplace.backend.model.PromoCode;

import com.marketplace.backend.model.Product.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "promo_codes")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromoCode {
    private String id;
    @Indexed(unique = true)
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