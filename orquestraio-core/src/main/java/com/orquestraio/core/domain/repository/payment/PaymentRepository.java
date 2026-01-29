package com.orquestraio.core.domain.repository.payment;

import com.orquestraio.core.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findById(UUID uuid);

}
