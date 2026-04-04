package ru.iprody.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateChargeResponseDto {
    private UUID id;
    private BigDecimal amount;
    private String currency;
    private BigDecimal amountReceived;
    private String createdAt;
    private String chargedAt;
    private String customer;
    private UUID order;
    private String receiptEmail;
    private String status;
    private Map<String, Object> metadata;
}