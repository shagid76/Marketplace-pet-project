package com.marketplace.backend.service;

import com.marketplace.backend.model.Payment.Payment;
import com.marketplace.backend.model.Payment.Status;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.Product.ProductDocument;
import com.marketplace.backend.model.Product.ProductStatus;
import com.marketplace.backend.repository.PaymentRepository;
import com.marketplace.backend.repository.ProductRepository;
import com.marketplace.backend.repository.ProductSearchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSchedulerTest {

	@Mock ProductRepository productRepository;
	@Mock ProductSearchRepository productSearchRepository;
	@Mock PaymentRepository paymentRepository;

	@InjectMocks ProductScheduler scheduler;

	@Test
	void cancelExpiredPayments_pendingPayment_releasesLockAndMarksFailed() {
		Product locked = Product.builder()
				.id("p-1")
				.productStatus(ProductStatus.HIDDEN)
				.lockedAt(LocalDateTime.now().minusMinutes(15))
				.lockedByPaymentId("pay-1")
				.build();
		Payment payment = Payment.builder().id("pay-1").status(Status.PENDING).build();
		ProductDocument doc = ProductDocument.builder().id("p-1").locked(true).build();

		when(productRepository.findAllByProductStatusAndLockedAtBefore(any(), any()))
				.thenReturn(List.of(locked));
		when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));
		when(productSearchRepository.findById("p-1")).thenReturn(Optional.of(doc));

		scheduler.cancelExpiredPayments();

		assertThat(locked.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);
		assertThat(locked.getLockedByPaymentId()).isNull();
		assertThat(locked.getLockedAt()).isNull();
		assertThat(doc.isLocked()).isFalse();
		assertThat(payment.getStatus()).isEqualTo(Status.FAILED);
		verify(productRepository).saveAll(List.of(locked));
		verify(productSearchRepository).saveAll(List.of(doc));
		verify(paymentRepository).saveAll(List.of(payment));
	}

	@Test
	void cancelExpiredPayments_paidPayment_doesNotResurrectProduct() {
		Product sold = Product.builder()
				.id("p-1")
				.productStatus(ProductStatus.HIDDEN)
				.lockedAt(LocalDateTime.now().minusMinutes(15))
				.lockedByPaymentId("pay-1")
				.build();
		Payment paid = Payment.builder().id("pay-1").status(Status.PAID).build();

		when(productRepository.findAllByProductStatusAndLockedAtBefore(any(), any()))
				.thenReturn(List.of(sold));
		when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(paid));

		scheduler.cancelExpiredPayments();

		verify(productRepository, never()).save(any());
		verify(paymentRepository, never()).save(any());
		assertThat(sold.getProductStatus()).isEqualTo(ProductStatus.HIDDEN);
	}

	@Test
	void cancelExpiredPayments_emptyResult_doesNothing() {
		when(productRepository.findAllByProductStatusAndLockedAtBefore(any(), any()))
				.thenReturn(List.of());

		scheduler.cancelExpiredPayments();

		verify(productRepository, never()).save(any());
		verify(productSearchRepository, never()).save(any());
	}
}
