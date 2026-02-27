package com.iprody.payment.service.app.persistence.service.dto;

import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreatePaymentDto {

    private UUID inquiryRefId;

    private BigDecimal amount;

    private String currency;

    private UUID transactionRefId;

    private PaymentStatus status;

    private String note;
}