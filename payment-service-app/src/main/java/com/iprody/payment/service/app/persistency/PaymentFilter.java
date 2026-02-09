package com.iprody.payment.service.app.persistency;

import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentFilter {
    private PaymentStatus status;
    private String currency;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Instant createdAfter;
    private Instant createdBefore;
}