package com.ozpay.infra.persistence;

import com.ozpay.domain.entity.Payment;
import com.ozpay.domain.repository.payment.PaymentRepository;
import com.ozpay.infra.persistence.entity.PaymentEntity;
import com.ozpay.infra.persistence.mapper.PaymentMapper;
import com.ozpay.infra.persistence.repository.payment.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PaymentPersistenceGateway implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentMapper paymentMapper;

    public Payment save (Payment payment){
        PaymentEntity entity = paymentMapper.toEntity(payment);
        PaymentEntity savePayment = paymentJpaRepository.save(entity);

        return paymentMapper.toDomain(savePayment);
    }


    @Override
    public Optional<Payment> findById(UUID uuid) {
        Optional<PaymentEntity> entity = paymentJpaRepository.findById(uuid);

        return entity.map(paymentMapper::toDomain);
    }
}
