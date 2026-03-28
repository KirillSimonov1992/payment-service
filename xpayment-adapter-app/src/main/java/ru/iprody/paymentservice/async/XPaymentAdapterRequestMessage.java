package ru.iprody.paymentservice.async;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Сообщение-запрос для платёжной системы X Payment.
 * <p>
 *     Используется для передачи информации о платеже, включая идентификаторы,
 *     сумму, валюту и время возникновения события.
 *     Реализует интерфейс {@link Message}, обеспечивая уникальный идентификатор сообщения
 *     и метку времени его возникновения.
 * </p>
 */
@Data
public class XPaymentAdapterRequestMessage implements Message {

    /**
     * Уникальный идентификатор платежа.
     */
    private UUID paymentGuid;

    /**
     * Сумма платежа.
     */
    private BigDecimal amount;

    /**
     * Валюта платежа в формате ISO 4217 (например, "USD", "EUR").
     */
    private String currency;

    /**
     * Момент времени, когда событие произошло.
     */
    private Instant occurredAt;

    @Override
    public UUID getMessageId() {
        return paymentGuid;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}