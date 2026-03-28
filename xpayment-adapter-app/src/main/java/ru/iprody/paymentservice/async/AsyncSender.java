package ru.iprody.paymentservice.async;

/**
 * Интерфейс отправки сообщения для асинхронной обработки.
 *
 * @param <T> тип сообщения, которое отправляется.
 */
public interface AsyncSender<T extends Message> {

    /**
     * Отправляет сообщение.
     *
     * @param message сообщение для отправки.
     */
    void send(T message);
}