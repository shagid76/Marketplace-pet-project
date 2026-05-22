package com.marketplace.backend.service;

import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.PromoCodeDto;
import com.marketplace.backend.exception.BadRequestException;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.exception.PromoCodeException;
import com.marketplace.backend.mapper.PageMapper;
import com.marketplace.backend.mapper.PromoCodeMapper;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.PromoCode.*;
import com.marketplace.backend.repository.ProductRepository;
import com.marketplace.backend.repository.PromoCodeRepository;
import com.marketplace.backend.repository.PromoCodeUsageRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.param.CouponCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromoCodeService {
    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;
    private final ProductRepository productRepository;
    private final PromoCodeMapper promoCodeMapper;
    private final PageMapper pageMapper;

    private Page<PromoCodeDto> findPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return promoCodeRepository.findAll(pageable)
                .map(promoCodeMapper::mapToDto);
    }

    public PageResponseDto<PromoCodeDto> findAll(int page, int size) {
        Page<PromoCodeDto> pageDto = findPage(page, size);
        return pageMapper.mapToDto(page, size, pageDto);
    }

    public PromoCodeDto create(CreatePromoCodeRequest createPromoCodeRequest) {
        if (promoCodeRepository.existsByCode(createPromoCodeRequest.getCode())) {
            throw new BadRequestException("Promo code already exists");
        }
        PromoCode promoCode = createPromoCode(createPromoCodeRequest);
        log.info("Promo code created: code={}, type={}, value={}",
                createPromoCodeRequest.getCode(), createPromoCodeRequest.getPromoCodeType(),
                createPromoCodeRequest.getDiscountValue());
        return promoCodeMapper.mapToDto(promoCode);
    }

    private PromoCode createPromoCode(CreatePromoCodeRequest createPromoCodeRequest) {
        String stripeCouponId = createStripeCoupon(createPromoCodeRequest);
        return promoCodeRepository.save(PromoCode.builder()
                .id(UUID.randomUUID().toString())
                .code(createPromoCodeRequest.getCode())
                .stripeCouponId(stripeCouponId)
                .promoCodeType(createPromoCodeRequest.getPromoCodeType())
                .startAt(createPromoCodeRequest.getStartAt())
                .endAt(createPromoCodeRequest.getEndAt())
                .applicableCategories(createPromoCodeRequest.getApplicableCategories())
                .discountValue(createPromoCodeRequest.getDiscountValue())
                .maxUsagePerUser(createPromoCodeRequest.getMaxUsagePerUser())
                .requiredPrice(createPromoCodeRequest.getRequiredPrice())
                .requiredProducts(createPromoCodeRequest.getRequiredProducts())
                .active(true)
                .build());
    }

    private String createStripeCoupon(CreatePromoCodeRequest request) {
        try {
            CouponCreateParams.Builder paramsBuilder = CouponCreateParams.builder()
                    .setName(request.getCode())
                    .setDuration(CouponCreateParams.Duration.ONCE);

            if (request.getPromoCodeType().equals(PromoCodeType.PERCENTAGE)) {
                paramsBuilder.setPercentOff(BigDecimal.valueOf(request.getDiscountValue()));
            } else {
                paramsBuilder.setAmountOff(
                        BigDecimal.valueOf(request.getDiscountValue())
                                .multiply(BigDecimal.valueOf(100))
                                .longValue()
                );
                paramsBuilder.setCurrency("usd");
            }

            Coupon coupon = Coupon.create(paramsBuilder.build());
            return coupon.getId();
        } catch (StripeException e) {
            throw new PromoCodeException(PromoCodeErrorCode.INVALID_CODE, "Error creating coupon in Stripe: " + e.getMessage());
        }
    }

    private PromoCode findByName(String code) {
        return promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Promo code with code: " + code + " not found"));
    }

    private PromoCode findById(String id) {
        return promoCodeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promo code with id: " + id + " not found"));
    }

    public PromoCodeDto findByCode(String code) {
        return promoCodeMapper.mapToDto(findByName(code));
    }

    public PromoCodeDto checkPromoCode(CheckPromoCodeRequest checkPromoCodeRequest, String currentUserId) {
        List<Product> products = productRepository.findAllById(checkPromoCodeRequest.getProductIds());
        if (products.isEmpty()) {
            throw new PromoCodeException(PromoCodeErrorCode.NO_PRODUCTS,
                    "No products provided");
        }

        PromoCode promoCode = findByName(checkPromoCodeRequest.getCode());
        validateActive(promoCode);
        validateDates(promoCode, LocalDateTime.now());
        validateMinProducts(promoCode, products.size());

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Product product : products) {
            validateApplicableCategories(promoCode, product);
            totalPrice = totalPrice.add(product.getPrice());
        }

        validateMinTotal(promoCode, totalPrice);
        int alreadyUsed = promoCodeUsageRepository.countByUserIdAndPromoCodeId(currentUserId, promoCode.getId());
        validateUserUsage(promoCode, alreadyUsed);

        return promoCodeMapper.mapToDto(promoCode);
    }

    private void validateActive(PromoCode promoCode) {
        if (!promoCode.isActive()) {
            throw new PromoCodeException(PromoCodeErrorCode.NOT_ACTIVE,
                    "Promo code with code: " + promoCode.getCode() + " not active");
        }
    }

    private void validateDates(PromoCode promoCode, LocalDateTime now) {
        if (!now.isBefore(promoCode.getEndAt())) {
            throw new PromoCodeException(PromoCodeErrorCode.EXPIRED,
                    "Promo code '" + promoCode.getCode() + "' has expired");
        }
        if (!now.isAfter(promoCode.getStartAt())) {
            throw new PromoCodeException(PromoCodeErrorCode.NOT_STARTED,
                    "Promo code '" + promoCode.getCode() + "' has not started yet");
        }
    }

    private void validateMinProducts(PromoCode promoCode, int size) {
        if (promoCode.getRequiredProducts() > size) {
            throw new PromoCodeException(PromoCodeErrorCode.MIN_PRODUCTS_NOT_MET,
                    "For this promo code you don't have enough products! Required products: "
                            + promoCode.getRequiredProducts());
        }
    }

    private void validateApplicableCategories(PromoCode promoCode,
                                              Product product) {
        if (!promoCode.getApplicableCategories().contains(product.getCategory())) {
            throw new PromoCodeException(PromoCodeErrorCode.NOT_APPLICABLE,
                    "Promo code with code: " + promoCode.getCode() + " is not applicable!");
        }
    }

    private void validateMinTotal(PromoCode promoCode, BigDecimal totalPrice) {
        if (totalPrice.compareTo(promoCode.getRequiredPrice()) < 0) {
            throw new PromoCodeException(PromoCodeErrorCode.MIN_ORDER_AMOUNT_NOT_MET,
                    "For this promo code, required price is: " + promoCode.getRequiredPrice());
        }
    }

    private void validateUserUsage(PromoCode promoCode, int alreadyUsed) {
        if (alreadyUsed >= promoCode.getMaxUsagePerUser()) {
            throw new PromoCodeException(PromoCodeErrorCode.USAGE_LIMIT_EXCEEDED,
                    "For this promo code, used count is: " + promoCode.getMaxUsagePerUser() + ", you already use it " +
                            alreadyUsed + " times!");
        }
    }

    public PromoCodeDto deactivate(String id) {
        PromoCode promoCode = findById(id);
        log.info("Promo code deactivated: id={}, code={}", id, promoCode.getCode());
        if (promoCode.getStripeCouponId() != null) {
            try {
                promoCode.setActive(false);
                promoCodeRepository.save(promoCode);

                Coupon resource = Coupon.retrieve(promoCode.getStripeCouponId());
                resource.delete();
            } catch (StripeException e) {
                promoCode.setActive(true);
                promoCodeRepository.save(promoCode);
                throw new NotFoundException("Coupon with id: " + id + " not found");
            }
        }
        return promoCodeMapper.mapToDto(promoCode);
    }
}
