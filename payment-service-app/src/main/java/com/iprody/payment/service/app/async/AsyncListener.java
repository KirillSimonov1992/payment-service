package com.iprody.payment.service.app.async;

/**
 * Интерфейс слушателя входящих сообщений.
 *
 * @param <T> тип сообщения, который обрабатывается.
 */
interface AsyncListener<T extends Message> {

    /**
     * Вызывается для каждого нового входящего сообщения.
     *
     * @param message сообщение для обработки.
     */
    void onMessage(T message);
}