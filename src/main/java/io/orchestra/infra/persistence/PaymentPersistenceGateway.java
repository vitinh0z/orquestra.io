package io.orchestra.infra.persistence;

import io.orchestra.domain.entity.Payment;
import io.orchestra.domain.repository.payment.PaymentRepository;
import io.orchestra.infra.persistence.entity.PaymentEntity;
import io.orchestra.infra.persistence.mapper.PaymentMapper;
import io.orchestra.infra.persistence.repository.payment.PaymentJpaRepository;
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
