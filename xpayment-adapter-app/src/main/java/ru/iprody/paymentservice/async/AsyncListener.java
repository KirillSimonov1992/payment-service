package ru.iprody.paymentservice.async;

/**
 * Интерфейс слушателя входящих сообщений.
 *
 * @param <T> тип сообщения, который обрабатывается.
 */
public interface AsyncListener<T extends Message> {

    /**
     * Вызывается для каждого нового входящего сообщения.
     *
     * @param message сообщение для обработки.
     */
    void onMessage(T message);
}