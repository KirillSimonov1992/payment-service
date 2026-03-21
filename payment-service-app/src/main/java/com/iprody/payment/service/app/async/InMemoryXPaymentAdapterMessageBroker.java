package com.iprody.payment.service.app.async;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.iprody.payment.service.app.async.XPaymentAdapterStatus.CANCELED;
import static com.iprody.payment.service.app.async.XPaymentAdapterStatus.SUCCEEDED;

@Slf4j
@Service
class InMemoryXPaymentAdapterMessageBroker
        implements AsyncSender<XPaymentAdapterRequestMessage> {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final AsyncListener<XPaymentAdapterResponseMessage> resultListener;
    private final BigDecimal two = BigDecimal.valueOf(2);

    @Autowired
    public InMemoryXPaymentAdapterMessageBroker(
            AsyncListener<XPaymentAdapterResponseMessage> resultListener) {
        this.resultListener = resultListener;
    }

    @Override
    public void send(XPaymentAdapterRequestMessage request) {
        UUID txId = UUID.randomUUID();
        log.info("Sending XPaymentAdapterRequestMessage to async listener: {}", txId);
        scheduler.schedule(() -> emit(request, txId), 20, TimeUnit.SECONDS);
    }

    private void emit(XPaymentAdapterRequestMessage request, UUID txId) {
        XPaymentAdapterResponseMessage result = new XPaymentAdapterResponseMessage();
        result.setPaymentGuid(request.getPaymentGuid());
        request.setAmount(request.getAmount());
        result.setCurrency(request.getCurrency());
        result.setTransactionRefId(txId);
        result.setOccurredAt(Instant.now());

        if (request.getAmount().remainder(two).compareTo(BigDecimal.ZERO) == 0) {
            result.setStatus(SUCCEEDED);
        } else {
            result.setStatus(CANCELED);
        }
        log.info("Emit response: {}", result);
        resultListener.onMessage(result);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
}