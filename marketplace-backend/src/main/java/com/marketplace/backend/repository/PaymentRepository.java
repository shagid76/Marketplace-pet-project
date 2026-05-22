package com.marketplace.backend.repository;

import com.marketplace.backend.model.Payment.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
}
