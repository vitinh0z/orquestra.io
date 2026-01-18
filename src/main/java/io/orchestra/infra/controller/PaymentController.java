package io.orchestra.infra.controller;

import io.orchestra.application.dto.PaymentRequestDTO;
import io.orchestra.application.dto.PaymentResponseDTO;
import io.orchestra.application.usecase.ProcessPaymentUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/")
@RequiredArgsConstructor
public class PaymentController {

    private final ProcessPaymentUseCase usecase;

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponseDTO> payment (@Valid @RequestBody PaymentRequestDTO dto,
                                                       UriComponentsBuilder uri
    ){
        PaymentResponseDTO response = usecase.execute(dto);

        URI url = uri.path("/v1/payments/{id}")
                .buildAndExpand(response.paymentId())
                .toUri();

        return ResponseEntity.created(url).body(response);
    }

}
