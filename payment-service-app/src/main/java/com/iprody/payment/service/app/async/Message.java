package com.iprody.payment.service.app.async;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * Интерфейс, представляющий сообщение с уникальным
 * идентификатором и временем возникновения.
 */
public interface Message {
    /**
     * Возвращает уникальный идентификатор сообщения.
     *
     * @return UUID сообщения.
     */
    UUID getMessageId();

    /**
     * Возвращает время возникновения сообщения.
     *
     * @return момент времени возникновения сообщения.
     */
    Instant getOccurredAt();
}