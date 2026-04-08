package ru.iprody.paymentservice.checkstate;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentStateCheckRegistrar {

    void register(
            UUID chargeGuid,
            UUID paymentGUID,
            BigDecimal amount,
            String currency
    );
}