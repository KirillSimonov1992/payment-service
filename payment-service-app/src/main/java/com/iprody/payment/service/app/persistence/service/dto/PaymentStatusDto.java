package com.iprody.payment.service.app.persistence.service.dto;

import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

@Data
@AllArgsConstructor
public class PaymentStatusDto {
    @NotNull
    private PaymentStatus paymentStatus;
}