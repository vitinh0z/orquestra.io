package io.orchestra.application.usecase;

import io.orchestra.application.dto.PaymentRequestDTO;
import io.orchestra.application.dto.PaymentResponseDTO;
import io.orchestra.domain.entity.Payment;
import io.orchestra.domain.repository.payment.PaymentRepository;
import io.orchestra.infra.gateway.PaymentGateway;
import io.orchestra.infra.persistence.mapper.PaymentRequestDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentRequestDtoMapper paymentMapper;


    public PaymentResponseDTO execute(PaymentRequestDTO payment){

        Payment domain = paymentMapper.toDomain(payment);
        Payment processPayment = paymentGateway.process(domain);
        Payment savePayment = paymentRepository.save(processPayment);
        return paymentMapper.toDto(savePayment);
    }

}
