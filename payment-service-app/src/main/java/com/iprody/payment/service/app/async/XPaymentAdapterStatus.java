package com.iprody.payment.service.app.async;

/**
 * Статусы, в которых может пребывать платежная
 * транзакция XPaymentAdapter.
 */
public enum XPaymentAdapterStatus {
    PROCESSING,
    CANCELED,
    SUCCEEDED
}