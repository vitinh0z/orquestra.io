package io.orchestra.infra.persistence.mapper;

import io.orchestra.application.dto.GatewayDetails;
import io.orchestra.application.dto.PaymentRequestDTO;
import io.orchestra.application.dto.PaymentResponseDTO;
import io.orchestra.domain.entity.Payment;
import io.orchestra.domain.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentRequestDtoMapper {

    public Payment toDomain (PaymentRequestDTO dto){
        if (dto == null) return null;

        return new Payment(
                null,
                null,
                dto.idempotecyKey(),
                dto.amount(),
                PaymentStatus.PENDING,
                dto.currency(),
                null, // gatewayTransactionId é gerado após o processamento
                LocalDateTime.now(),
                null, // qrCode é gerado após o processamento
                null, // qrCodeBase64 é gerado após o processamento
                dto.customer().email()
        );
    }

    public PaymentResponseDTO toDto (Payment domain){
        if (domain == null) return null;

        var details = new GatewayDetails(domain.getGatewayTransactionId());

        return new PaymentResponseDTO(
                domain.getId(),
                domain.getStatus().toString(),
                domain.getMoney(),
                domain.getCurrency(),
                details,
                domain.getCreatedAt(),
                domain.getQrCode(),
                domain.getQrCodeBase64(),
                domain.getCustomerEmail()
        );
    }
}
