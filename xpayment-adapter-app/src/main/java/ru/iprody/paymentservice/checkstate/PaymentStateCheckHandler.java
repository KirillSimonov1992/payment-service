package ru.iprody.paymentservice.checkstate;

import java.util.UUID;

public interface PaymentStateCheckHandler {
    boolean handle(UUID chargeGuid);
}