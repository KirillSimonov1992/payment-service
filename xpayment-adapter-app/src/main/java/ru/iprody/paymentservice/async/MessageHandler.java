package ru.iprody.paymentservice.async;

/**
 * Интерфейс обработчика входящих сообщений.
 *
 * @param <T> тип сообщения, который обрабатывается.
 */
public interface MessageHandler<T extends Message> {

    /**
     * Обрабатывает переданное сообщение.
     *
     * @param message сообщение для обработки.
     */
    void handleMessage(T message);
}