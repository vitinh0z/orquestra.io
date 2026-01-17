package com.ozpay.domain.repository.payment;

import com.ozpay.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID uuid);

}
