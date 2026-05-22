package com.marketplace.backend.controller;

import com.marketplace.backend.service.CartService;
import com.marketplace.backend.service.PaymentService;
import com.marketplace.backend.service.ProductService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final PaymentService paymentService;
    private final ProductService productService;
    private final CartService cartService;

    @Value("${stripe.webhookSecret}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        try {
            switch (event.getType()) {
                case "checkout.session.completed" -> handleCompleted(event);
                case "checkout.session.expired",
                     "checkout.session.async_payment_failed",
                     "payment_intent.payment_failed" -> handleExpired(event);
            }
            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            return ResponseEntity.ok("Webhook processed with internal error");
        }
    }

    private void handleCompleted(Event event) {
        Session session = extractSession(event);
        if (session == null) return;

        String paymentId = session.getMetadata().get("paymentId");
        String buyerId = session.getMetadata().get("buyerId");
        String isFromCart = session.getMetadata().get("isFromCart");
        String promoCodeId = session.getMetadata().get("promoCodeId");

        List<String> productIds = paymentService.markPaymentAsPaid(paymentId);
        if ("true".equals(isFromCart)) {
            cartService.clearCart(buyerId);
        }
        productService.buy(productIds, buyerId, promoCodeId);
    }

    private void handleExpired(Event event) {
        Session session = extractSession(event);
        if (session == null) return;
        String paymentId = session.getMetadata().get("paymentId");
        paymentService.unlockProductsForExpiredPayment(paymentId);
    }

    private Session extractSession(Event event) {
        EventDataObjectDeserializer d = event.getDataObjectDeserializer();
        if (d.getObject().isPresent()) {
            return (Session) d.getObject().get();
        }
        try {
            return (Session) d.deserializeUnsafe();
        } catch (Exception e) {
            return null;
        }
    }
}