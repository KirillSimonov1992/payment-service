package ru.iprody.paymentservice.async;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private final AsyncSender<XPaymentAdapterResponseMessage> sender;
    private final AsyncSender<XPaymentAdapterRequestMessage> senderDLT;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final BigDecimal two = new BigDecimal("2");

    @Autowired
    public RequestMessageHandler(
            AsyncSender<XPaymentAdapterResponseMessage> sender,
            AsyncSender<XPaymentAdapterRequestMessage> senderDLT
    ) {
        this.sender = sender;
        this.senderDLT = senderDLT;
    }

    @Override
    public void handleMessage(XPaymentAdapterRequestMessage message) {
        scheduler.schedule(() -> {
            log.info("Sending response about processing request {}", message.getPaymentGuid());

            if (!validation(message)) {
                senderDLT.send(message);
                return;
            }

            BigDecimal amount = message.getAmount();
            XPaymentAdapterStatus status = XPaymentAdapterStatus.CANCELED;
            if (amount.remainder(two).compareTo(BigDecimal.ZERO) == 0) {
                status = XPaymentAdapterStatus.SUCCEEDED;
            }

            XPaymentAdapterResponseMessage response = new XPaymentAdapterResponseMessage();
            response.setMessageGuid(message.getMessageId());
            response.setPaymentGuid(message.getPaymentGuid());
            response.setAmount(amount);
            response.setCurrency(message.getCurrency());
            response.setStatus(status);
            response.setTransactionRefId(UUID.randomUUID());
            response.setOccurredAt(Instant.now());

            sender.send(response);
        }, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }

    private boolean validation(XPaymentAdapterRequestMessage message) {
        boolean isValid = true;
        if (message.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            isValid = false;
        }
        if (message.getCurrency() == null) {
            isValid = false;
        }
        return isValid;
    }
}