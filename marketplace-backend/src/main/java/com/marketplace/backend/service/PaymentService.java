package com.marketplace.backend.service;

import com.marketplace.backend.exception.ForbiddenException;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.exception.PaymentException;
import com.marketplace.backend.model.Payment.CreatePaymentRequest;
import com.marketplace.backend.model.Payment.Payment;
import com.marketplace.backend.model.Payment.Status;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.Product.ProductDocument;
import com.marketplace.backend.model.Product.ProductStatus;
import com.marketplace.backend.repository.PaymentRepository;
import com.marketplace.backend.repository.ProductRepository;
import com.marketplace.backend.repository.ProductSearchRepository;
import com.marketplace.backend.repository.PromoCodeRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final ProductSearchRepository productSearchRepository;

    @Value("${app.frontend-origin}")
    private String origin;

    @Value("${stripe.apiKey}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.stripeApiKey;
    }

    @Transactional
    public String createStripeSessionId(CreatePaymentRequest request, String buyerId, String promo) {
        List<Product> products = reserveProducts(request);

        Payment payment = createPayment(request, buyerId);

        lockProductsByPayment(payment.getId(), products);

        SessionCreateParams params = buildStripeParams(products, payment, buyerId, request, promo);
        Session session = null;
        try {
            session = createStripeSession(params);
        } catch (StripeException e) {
            log.error("Failed to create Stripe session for buyer={}: {}", buyerId, e.getMessage(), e);
            throw new PaymentException("Failed to create Stripe session: " + e.getMessage(), e);
        }

        payment.setStatus(Status.PENDING);
        payment.setStripeSessionId(session.getId());
        paymentRepository.save(payment);
        return session.getUrl();
    }

    private Payment createPayment(CreatePaymentRequest request, String buyerId) {
        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .productId(request.getProductId())
                .buyerId(buyerId)
                .status(Status.CREATE)
                .build();
        return paymentRepository.save(payment);
    }

    private void lockProductsByPayment(String paymentId, List<Product> products) {
        products.forEach(product -> product.setLockedByPaymentId(paymentId));
        productRepository.saveAll(products);
    }

    private List<Product> reserveProducts(CreatePaymentRequest request) {
        List<Product> products = productRepository.findAllById(request.getProductId());
        validateProducts(products, request);
        List<ProductDocument> productDocuments = productSearchRepository.findAllById(request.getProductId());

        try {
            lockProductDocuments(productDocuments);
            return lockProducts(products);
        } catch (org.springframework.dao.OptimisticLockingFailureException |
                 org.springframework.dao.DuplicateKeyException e) {
            throw new IllegalStateException("The item has already been purchased or reserved by another user..");
        }
    }

    private List<Product> lockProducts(List<Product> products) {
        products.forEach(product -> {
            product.setProductStatus(ProductStatus.HIDDEN);
            product.setLockedAt(LocalDateTime.now());
        });
        return productRepository.saveAll(products);
    }

    private void lockProductDocuments(List<ProductDocument> productDocuments) {
        productDocuments.forEach(productDocument -> {
            productDocument.setLocked(true);
        });
        productSearchRepository.saveAll(productDocuments);
    }

    private SessionCreateParams buildStripeParams(List<Product> products, Payment payment, String buyerId, CreatePaymentRequest request, String promo) {
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(origin + "/me?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(origin)
                .putMetadata("paymentId", payment.getId())
                .putMetadata("buyerId", buyerId)
                .putMetadata("isFromCart", String.valueOf(request.isInCart()));

        products.forEach(product -> paramsBuilder.addLineItem(mapToLineItem(product)));

        promoCodeRepository.findByCode(promo)
                .filter(p -> p.isActive() && p.getStripeCouponId() != null)
                .ifPresent(p -> {
                    paramsBuilder.addDiscount(
                            SessionCreateParams.Discount.builder()
                                    .setCoupon(p.getStripeCouponId())
                                    .build()
                    );
                    paramsBuilder.putMetadata("promoCodeId", p.getId());
                });

        return paramsBuilder.build();
    }

    private SessionCreateParams.LineItem mapToLineItem(Product product) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(product.getPrice().multiply(BigDecimal.valueOf(100)).longValue())
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(product.getTitle()).build())
                        .build())
                .build();
    }

    private void validateProducts(List<Product> products, CreatePaymentRequest request) {
        if (products.isEmpty()) {
            throw new NotFoundException("Product not found");
        }

        if (products.size() != request.getProductId().size()) {
            throw new NotFoundException("Some products not found");
        }

        for (Product product : products) {
            if (!product.isInStock()) {
                throw new IllegalStateException("Product already sold: " + product.getId());
            }
            if (product.getProductStatus().equals(ProductStatus.BANNED)) {
                throw new ForbiddenException("Action impossible: product is banned.");
            }
            if (product.getProductStatus() == ProductStatus.HIDDEN ||
                    product.getProductStatus() == ProductStatus.DELETED) {
                throw new IllegalStateException("Product not available: " + product.getId());
            }
        }
    }

    public List<String> markPaymentAsPaid(String paymentId) {
        log.info("Payment marked as paid: paymentId={}", paymentId);
        Payment payment = findById(paymentId);
        if (payment.getStatus() != Status.PAID) {
            payment.setStatus(Status.PAID);
            paymentRepository.save(payment);
        }
        return payment.getProductId();
    }

    private Payment findById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
    }

    @Transactional
    public void unlockProductsForExpiredPayment(String paymentId) {
        log.info("Unlocking products for expired payment: paymentId={}", paymentId);
        Payment payment = findById(paymentId);
        if (payment.getStatus() == Status.PAID || payment.getStatus() == Status.FAILED) {
            return;
        }

        payment.setStatus(Status.FAILED);
        paymentRepository.save(payment);

        List<String> productIds = new ArrayList<>();
        List<Product> products = productRepository.findAllById(payment.getProductId());
        products.forEach(product -> {
            product.setProductStatus(ProductStatus.ACTIVE);
            product.setLockedByPaymentId(null);
            product.setLockedAt(null);
            productIds.add(product.getId());
        });
        productRepository.saveAll(products);
        Iterable<ProductDocument> docs = productSearchRepository.findAllById(productIds);
        for (ProductDocument doc : docs) {
            doc.setLocked(false);
        }
        productSearchRepository.saveAll(docs);

    }

    protected Session createStripeSession(SessionCreateParams params) throws StripeException {
        return Session.create(params);
    }
}