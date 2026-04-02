package ru.iprody.paymentservice.async;

/**
 * Статусы, в которых может пребывать платежная
 * транзакция XPaymentAdapter.
 */
public enum XPaymentAdapterStatus {
    PROCESSING,
    CANCELED,
    SUCCEEDED
}