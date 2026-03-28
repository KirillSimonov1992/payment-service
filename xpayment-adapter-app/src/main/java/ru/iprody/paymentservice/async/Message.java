package ru.iprody.paymentservice.async;

import java.time.Instant;
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