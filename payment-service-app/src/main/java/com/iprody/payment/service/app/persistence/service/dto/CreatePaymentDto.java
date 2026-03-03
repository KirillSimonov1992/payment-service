package com.iprody.payment.service.app.persistence.service.dto;

import com.iprody.payment.service.app.persistence.entity.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentDto(
        UUID inquiryRefId,
        BigDecimal amount,
        String currency,
        UUID transactionRefId,
        PaymentStatus status,
        String note
) {}