package com.marketplace.backend.controller;

import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.PromoCodeDto;
import com.marketplace.backend.model.PromoCode.CheckPromoCodeRequest;
import com.marketplace.backend.model.PromoCode.CreatePromoCodeRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.PromoCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/promo_codes")
@Tag(name = "Promo Codes", description = "Discount code creation, listing, and validation")
@SecurityRequirement(name = "bearerAuth")
public class PromoCodeController {
    private final PromoCodeService promoCodeService;

    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN')")
    @Operation(summary = "Create a new promo code (admin/moderator only)")
    @PostMapping()
    public ResponseEntity<PromoCodeDto> create(@RequestBody @Valid CreatePromoCodeRequest createPromoCodeRequest) {
        PromoCodeDto promoCodeDto = promoCodeService.create(createPromoCodeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(promoCodeDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN')")
    @Operation(summary = "List all promo codes with pagination (admin/moderator only)")
    @GetMapping()
    public ResponseEntity<PageResponseDto<PromoCodeDto>> getAll(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size) {
        PageResponseDto<PromoCodeDto> promoCodes = promoCodeService.findAll(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(promoCodes);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Look up a promo code by its code string")
    @GetMapping("/{code}")
    public ResponseEntity<PromoCodeDto> getByCode(@PathVariable String code) {
        PromoCodeDto promoCodeDto = promoCodeService.findByCode(code);
        return ResponseEntity.status(HttpStatus.OK).body(promoCodeDto);
    }

    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_ADMIN')")
    @Operation(summary = "Deactivate a promo code (admin/moderator only)")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PromoCodeDto> deactivatePromoCode(@PathVariable String id) {
        PromoCodeDto promoCodeDto = promoCodeService.deactivate(id);
        return ResponseEntity.status(HttpStatus.OK).body(promoCodeDto);
    }

    @Operation(summary = "Validate a promo code against a list of product IDs")
    @PostMapping("/check")
    public ResponseEntity<PromoCodeDto> checkPromoCode(@RequestBody @Valid CheckPromoCodeRequest createPromoCodeRequest) {
        PromoCodeDto promoCodeDto = promoCodeService.checkPromoCode(createPromoCodeRequest,
                SecurityUtils.getCurrentUserIdOrThrow());
        return ResponseEntity.status(HttpStatus.OK).body(promoCodeDto);
    }
}
