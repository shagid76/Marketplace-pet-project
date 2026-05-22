package com.marketplace.backend.service;

import com.marketplace.backend.exception.ForbiddenException;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.model.Payment.Payment;
import com.marketplace.backend.model.Payment.Status;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.Product.ProductDocument;
import com.marketplace.backend.model.Product.ProductStatus;
import com.marketplace.backend.repository.PaymentRepository;
import com.marketplace.backend.repository.ProductRepository;
import com.marketplace.backend.repository.ProductSearchRepository;
import com.marketplace.backend.repository.PromoCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock ProductRepository productRepository;
    @Mock PromoCodeRepository promoCodeRepository;
    @Mock ProductSearchRepository productSearchRepository;

    @InjectMocks PaymentService paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "stripeApiKey", "sk_test_dummy");
        ReflectionTestUtils.setField(paymentService, "origin", "http://localhost:3000");
    }


    @Test
    void markPaymentAsPaid_pendingPayment_setsStatusPaidAndReturnsProductIds() {
        Payment payment = Payment.builder()
                .id("pay-1")
                .productId(List.of("p-1", "p-2"))
                .status(Status.PENDING)
                .build();
        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<String> result = paymentService.markPaymentAsPaid("pay-1");

        assertThat(result).containsExactly("p-1", "p-2");
        assertThat(payment.getStatus()).isEqualTo(Status.PAID);
        verify(paymentRepository).save(payment);
    }

    @Test
    void markPaymentAsPaid_alreadyPaid_doesNotSaveAgain() {
        Payment payment = Payment.builder()
                .id("pay-2")
                .productId(List.of("p-3"))
                .status(Status.PAID)
                .build();
        when(paymentRepository.findById("pay-2")).thenReturn(Optional.of(payment));

        List<String> result = paymentService.markPaymentAsPaid("pay-2");

        assertThat(result).containsExactly("p-3");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void markPaymentAsPaid_notFound_throwsNotFoundException() {
        when(paymentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.markPaymentAsPaid("missing"))
                .isInstanceOf(NotFoundException.class);
    }

    // ── unlockProductsForExpiredPayment ──────────────────────────────────────

    @Test
    void unlockProductsForExpiredPayment_pendingPayment_unlockProductsAndSetsFailed() {
        Payment payment = Payment.builder()
                .id("pay-3")
                .productId(List.of("p-10"))
                .status(Status.PENDING)
                .build();
        Product product = Product.builder()
                .id("p-10")
                .productStatus(ProductStatus.HIDDEN)
                .lockedByPaymentId("pay-3")
                .build();
        ProductDocument doc = ProductDocument.builder().id("p-10").locked(true).build();

        when(paymentRepository.findById("pay-3")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.findAllById(List.of("p-10"))).thenReturn(List.of(product));
        when(productSearchRepository.findAllById(List.of("p-10"))).thenReturn(List.of(doc));

        paymentService.unlockProductsForExpiredPayment("pay-3");

        assertThat(payment.getStatus()).isEqualTo(Status.FAILED);
        assertThat(product.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getLockedByPaymentId()).isNull();
        assertThat(product.getLockedAt()).isNull();
        assertThat(doc.isLocked()).isFalse();
    }

    @Test
    void unlockProductsForExpiredPayment_alreadyPaid_skips() {
        Payment payment = Payment.builder()
                .id("pay-4")
                .status(Status.PAID)
                .build();
        when(paymentRepository.findById("pay-4")).thenReturn(Optional.of(payment));

        paymentService.unlockProductsForExpiredPayment("pay-4");

        verify(productRepository, never()).findAllById(any());
    }

    @Test
    void unlockProductsForExpiredPayment_alreadyFailed_skips() {
        Payment payment = Payment.builder()
                .id("pay-5")
                .status(Status.FAILED)
                .build();
        when(paymentRepository.findById("pay-5")).thenReturn(Optional.of(payment));

        paymentService.unlockProductsForExpiredPayment("pay-5");

        verify(productRepository, never()).findAllById(any());
    }

    @Test
    void unlockProductsForExpiredPayment_productDocumentMissing_stillUnlocksProduct() {
        Payment payment = Payment.builder()
                .id("pay-6")
                .productId(List.of("p-20"))
                .status(Status.PENDING)
                .build();
        Product product = Product.builder()
                .id("p-20")
                .productStatus(ProductStatus.HIDDEN)
                .build();

        when(paymentRepository.findById("pay-6")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.findAllById(List.of("p-20"))).thenReturn(List.of(product));

        paymentService.unlockProductsForExpiredPayment("pay-6");

        assertThat(product.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    // ── createStripeSessionId - validation paths (no real Stripe call) ───────

    @Test
    void createStripeSessionId_emptyProductList_throwsNotFoundException() {
        com.marketplace.backend.model.Payment.CreatePaymentRequest req =
                new com.marketplace.backend.model.Payment.CreatePaymentRequest();
        req.setProductId(List.of("p-99"));
        when(productRepository.findAllById(List.of("p-99"))).thenReturn(List.of());

        assertThatThrownBy(() -> paymentService.createStripeSessionId(req, "buyer-1", null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createStripeSessionId_sizeMismatch_throwsNotFoundException() {
        com.marketplace.backend.model.Payment.CreatePaymentRequest req =
                new com.marketplace.backend.model.Payment.CreatePaymentRequest();
        req.setProductId(List.of("p-1", "p-2"));
        Product p1 = Product.builder().id("p-1").inStock(true)
                .productStatus(ProductStatus.ACTIVE).price(BigDecimal.TEN).build();
        when(productRepository.findAllById(any())).thenReturn(List.of(p1));

        assertThatThrownBy(() -> paymentService.createStripeSessionId(req, "buyer-1", null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Some products not found");
    }

    @Test
    void createStripeSessionId_outOfStock_throwsIllegalState() {
        com.marketplace.backend.model.Payment.CreatePaymentRequest req =
                new com.marketplace.backend.model.Payment.CreatePaymentRequest();
        req.setProductId(List.of("p-1"));
        Product p1 = Product.builder().id("p-1").inStock(false)
                .productStatus(ProductStatus.ACTIVE).build();
        when(productRepository.findAllById(any())).thenReturn(List.of(p1));

        assertThatThrownBy(() -> paymentService.createStripeSessionId(req, "buyer-1", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already sold");
    }

    @Test
    void createStripeSessionId_bannedProduct_throwsResponseStatusException() {
        com.marketplace.backend.model.Payment.CreatePaymentRequest req =
                new com.marketplace.backend.model.Payment.CreatePaymentRequest();
        req.setProductId(List.of("p-1"));
        Product p1 = Product.builder().id("p-1").inStock(true)
                .productStatus(ProductStatus.BANNED).build();
        when(productRepository.findAllById(any())).thenReturn(List.of(p1));

        assertThatThrownBy(() -> paymentService.createStripeSessionId(req, "buyer-1", null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createStripeSessionId_hiddenProduct_throwsIllegalState() {
        com.marketplace.backend.model.Payment.CreatePaymentRequest req =
                new com.marketplace.backend.model.Payment.CreatePaymentRequest();
        req.setProductId(List.of("p-1"));
        Product p1 = Product.builder().id("p-1").inStock(true)
                .productStatus(ProductStatus.HIDDEN).build();
        when(productRepository.findAllById(any())).thenReturn(List.of(p1));

        assertThatThrownBy(() -> paymentService.createStripeSessionId(req, "buyer-1", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");
    }
}
