package io.orchestra.core.domain.repository.payment;

import io.orchestra.core.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findById(UUID uuid);

}
