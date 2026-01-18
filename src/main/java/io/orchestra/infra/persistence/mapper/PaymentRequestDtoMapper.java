package io.orchestra.infra.persistence.mapper;

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
