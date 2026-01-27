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
                payment.getIdempotecyKey(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCurrency(),
                payment.getGatewayTransactionId(),
                payment.getCreatedAt(),
                payment.getQrCode(),
                payment.getQrCodeBase64(),
                payment.getCustomerEmail()
        );
    }

    public PaymentEntity toEntity(Payment payment){
        if (payment == null) return null;

        return new PaymentEntity(
                payment.getId(),
                payment.getTenantId(),
                payment.getIdempotencyKey(),
                payment.getMoney(),
                payment.getStatus(),
                payment.getCurrency(),
                payment.getCreatedAt(),
                payment.getQrCode(),
                payment.getQrCodeBase64(),
                payment.getGatewayTransactionId(),
                payment.getCustomerEmail()
        );
    }
}
