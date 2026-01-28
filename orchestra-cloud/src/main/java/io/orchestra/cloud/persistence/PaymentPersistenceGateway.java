package io.orchestra.cloud.infra.persistence;

import io.orchestra.core.domain.entity.Payment;
import io.orchestra.core.domain.repository.payment.PaymentRepository;
import io.orchestra.cloud.infra.persistence.entity.PaymentEntity;
import io.orchestra.cloud.infra.persistence.mapper.PaymentMapper;
import io.orchestra.cloud.infra.persistence.repository.payment.PaymentJpaRepository;
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
    public Optional<Payment> findByIdempotecyKey(String idempotecyKey) {
        Optional<PaymentEntity> entity = paymentJpaRepository.findByIdempotecyKey(idempotecyKey);

        return entity.map(paymentMapper::toDomain);
    }


    @Override
    public Optional<Payment> findById(UUID uuid) {
        Optional<PaymentEntity> entity = paymentJpaRepository.findById(uuid);

        return entity.map(paymentMapper::toDomain);
    }
}
