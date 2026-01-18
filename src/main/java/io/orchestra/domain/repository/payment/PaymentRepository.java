package io.orchestra.domain.repository.payment;

import io.orchestra.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID uuid);

}
