package io.orchestra.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payment {

    private UUID id;
    private UUID tenantId;
    private String idempotencyKey;
    private BigDecimal money;
    private PaymentStatus status;
    private String currency;
    private String gatewayTransactionId;
    private LocalDateTime createdAt;
    private String qrCode;
    private String qrCodeBase64;
    private String customerEmail;
    private String paymentMethod;
    private String ticketUrl;


    public boolean isPositive(BigDecimal amount){
        return getMoney().compareTo(amount) <= 0;
    }
}
