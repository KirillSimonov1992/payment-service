package ru.iprody.paymentservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateChargeRequestDto {
    private BigDecimal amount;
    private String currency;
    private String customer;
    private UUID order;
    private String receiptEmail;
    private Map<String, Object> metadata;
}