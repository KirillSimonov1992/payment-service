package ru.iprody.paymentservice.async;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import ru.iprody.paymentservice.api.XPaymentProviderGateway;
import ru.iprody.paymentservice.dto.CreateChargeRequestDto;
import ru.iprody.paymentservice.dto.CreateChargeResponseDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@AllArgsConstructor
public class RequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private final AsyncSender<XPaymentAdapterResponseMessage> sender;
    private final AsyncSender<XPaymentAdapterRequestMessage> senderDLT;
    private final XPaymentProviderGateway xPaymentProviderGateway;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private static final BigDecimal two = new BigDecimal("2");

    @Override
    public void handleMessage(XPaymentAdapterRequestMessage message) {
        log.info(
                "Payment request received paymentGuid - {},amount - {}, currency - {}",
                message.getPaymentGuid(), message.getAmount(), message.getCurrency()
        );

        executor.submit(() -> {
            int retries = 5;
            CreateChargeResponseDto chargeResponse = null;
            Exception lastException = null;
            while (retries > 0) {
                log.info("Retries left: {} for paymentGuid - {}", retries, message.getPaymentGuid());
                retries--;
                CreateChargeRequestDto createChargeRequest = new CreateChargeRequestDto();
                createChargeRequest.setAmount(message.getAmount());
                createChargeRequest.setCurrency(message.getCurrency());
                createChargeRequest.setOrder(message.getPaymentGuid());
                try {
                    chargeResponse = xPaymentProviderGateway.createCharge(createChargeRequest);
                } catch (RestClientException e) {
                    log.error("Error in time of sending payment request with paymentGuid - {}", message.getPaymentGuid(), e);
                    lastException = e;
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    log.error("Error creating charge for paymentGuid - {}", message.getPaymentGuid(), e);
                    lastException = e;
                    break;
                }
            }
            XPaymentAdapterResponseMessage responseMessage = new XPaymentAdapterResponseMessage();
            if (chargeResponse != null) {
                responseMessage.setPaymentGuid(chargeResponse.getOrder());
                responseMessage.setTransactionRefId(chargeResponse.getId());
                responseMessage.setAmount(chargeResponse.getAmount());
                responseMessage.setCurrency(chargeResponse.getCurrency());
                responseMessage.setStatus(XPaymentAdapterStatus.valueOf(chargeResponse.getStatus()));
                responseMessage.setOccurredAt(Instant.now());
            } else if (lastException != null) {
                responseMessage.setPaymentGuid(message.getPaymentGuid());
                responseMessage.setAmount(message.getAmount());
                responseMessage.setCurrency(message.getCurrency());
                responseMessage.setStatus(XPaymentAdapterStatus.CANCELED);
                responseMessage.setOccurredAt(Instant.now());
            } else {
                throw new IllegalStateException("Invalid state in message handler");
            }
            sender.send(responseMessage);
        });
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