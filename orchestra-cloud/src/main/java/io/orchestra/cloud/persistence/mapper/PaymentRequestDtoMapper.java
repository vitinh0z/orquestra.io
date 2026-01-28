package io.orchestra.cloud.infra.persistence.mapper;

import io.orchestra.core.application.dto.PaymentRequestDTO;
import io.orchestra.core.application.dto.PaymentResponseDTO;
import io.orchestra.core.domain.entity.Payment;
import io.orchestra.core.domain.entity.PaymentStatus;
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
                dto.paymentMethodRequest().token(),
                LocalDateTime.now()

        );
    }

    public PaymentResponseDTO toDto (Payment domain){
        if (domain == null) return null;

        return new PaymentResponseDTO(
                domain.getId(),
                domain.getStatus().toString(),
                domain.getMoney(),
                domain.getCurrency(),
                null,
                LocalDateTime.now()

        );


    }

}
