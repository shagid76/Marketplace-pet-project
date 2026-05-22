package com.marketplace.backend.mapper;

import com.marketplace.backend.dto.PromoCodeDto;
import com.marketplace.backend.model.PromoCode.PromoCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromoCodeMapper {
    public PromoCodeDto mapToDto(PromoCode promoCode) {
        return PromoCodeDto.builder()
                .id(promoCode.getId())
                .code(promoCode.getCode())
                .stripeCouponId(promoCode.getStripeCouponId())
                .promoCodeType(promoCode.getPromoCodeType())
                .startAt(promoCode.getStartAt())
                .endAt(promoCode.getEndAt())
                .discountValue(promoCode.getDiscountValue())
                .applicableCategories(promoCode.getApplicableCategories())
                .maxUsagePerUser(promoCode.getMaxUsagePerUser())
                .requiredProducts(promoCode.getRequiredProducts())
                .requiredPrice(promoCode.getRequiredPrice())
                .active(promoCode.isActive())
                .build();
    }
}