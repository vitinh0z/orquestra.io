package io.orchestra.core.domain.repository.payment;

import io.orchestra.core.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByIdempotecyKey(String idempotecyKey);

    Optional<Payment> findById(UUID uuid);

}
