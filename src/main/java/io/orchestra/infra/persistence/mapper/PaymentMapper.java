package io.orchestra.infra.persistence.mapper;

import io.orchestra.domain.entity.Payment;
import io.orchestra.infra.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toDomain(PaymentEntity payment){
        if(payment == null) return null;

        return new Payment(
                payment.getId(),
                payment.getTenantId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCurrency(),
                null,
                payment.getCreatedAt()
        );
    }

    public PaymentEntity toEntity(Payment payment){
        if (payment == null) return null;

        return new PaymentEntity(
                payment.getId(),
                payment.getTenantId(),
                payment.getMoney(),
                payment.getStatus(),
                payment.getCurrency(),
                payment.getCreatedAt()
        );
    }
}
