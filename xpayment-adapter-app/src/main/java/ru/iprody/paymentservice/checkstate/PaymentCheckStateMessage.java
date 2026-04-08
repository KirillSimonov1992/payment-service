package ru.iprody.paymentservice.checkstate;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCheckStateMessage(
        UUID chargeGuid,
        UUID paymentGUID,
        BigDecimal amount,
        String currency
) { }