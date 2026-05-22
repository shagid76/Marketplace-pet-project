package com.marketplace.backend.model.PromoCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "promo_code_usages")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeUsage {
    private String id;
    private String userId;
    private String promoCodeId;
    private LocalDateTime usageAt;
    private String orderId;
}
