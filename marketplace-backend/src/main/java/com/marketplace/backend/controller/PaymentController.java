package com.marketplace.backend.controller;

import com.marketplace.backend.model.Payment.CreatePaymentRequest;
import com.marketplace.backend.security.SecurityUtils;
import com.marketplace.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Stripe Checkout session creation")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "Create a Stripe Checkout session and return the redirect URL")
    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody @Valid CreatePaymentRequest createPaymentRequest,
                                         @RequestParam(required = false) String promoCode) {
        String url = paymentService.createStripeSessionId(createPaymentRequest, SecurityUtils.getCurrentUserIdOrThrow(), promoCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(url);
    }
}
