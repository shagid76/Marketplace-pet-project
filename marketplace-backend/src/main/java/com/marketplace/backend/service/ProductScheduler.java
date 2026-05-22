package com.marketplace.backend.service;

import com.marketplace.backend.model.Payment.Payment;
import com.marketplace.backend.model.Payment.Status;
import com.marketplace.backend.model.Product.Product;
import com.marketplace.backend.model.Product.ProductDocument;
import com.marketplace.backend.model.Product.ProductStatus;
import com.marketplace.backend.repository.PaymentRepository;
import com.marketplace.backend.repository.ProductRepository;
import com.marketplace.backend.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ProductScheduler {
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final PaymentRepository paymentRepository;


    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void cancelExpiredPayments() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(10);
        List<Product> expiredProducts = productRepository.findAllByProductStatusAndLockedAtBefore(
                ProductStatus.HIDDEN, timeout
        );

        if (expiredProducts.isEmpty()) return;

        List<Product> productsToUnlock = new ArrayList<>();
        List<ProductDocument> documentsToUpdate = new ArrayList<>();
        List<Payment> paymentsToUpdate = new ArrayList<>();

        for (Product product : expiredProducts) {

            Payment payment = product.getLockedByPaymentId() != null
                    ? paymentRepository.findById(product.getLockedByPaymentId()).orElse(null)
                    : null;

            if (payment != null && payment.getStatus() == Status.PAID) {
                continue;
            }

            if (payment != null && payment.getStatus() == Status.PENDING) {
                payment.setStatus(Status.FAILED);
                paymentsToUpdate.add(payment);
            }

            product.setProductStatus(ProductStatus.ACTIVE);
            product.setLockedByPaymentId(null);
            product.setLockedAt(null);

            productsToUnlock.add(product);

            productSearchRepository.findById(product.getId())
                    .ifPresent(doc -> {
                        doc.setLocked(false);
                        documentsToUpdate.add(doc);
                    });
        }

        if (!paymentsToUpdate.isEmpty()) {
            paymentRepository.saveAll(paymentsToUpdate);
        }

        if (!productsToUnlock.isEmpty()) {
            productRepository.saveAll(productsToUnlock);
        }

        if (!documentsToUpdate.isEmpty()) {
            productSearchRepository.saveAll(documentsToUpdate);
        }
    }
}