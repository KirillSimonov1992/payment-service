package ru.iprody.paymentservice.checkstate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.iprody.paymentservice.api.XPaymentProviderGateway;
import ru.iprody.paymentservice.async.AsyncSender;
import ru.iprody.paymentservice.async.XPaymentAdapterResponseMessage;
import ru.iprody.paymentservice.async.XPaymentAdapterStatus;
import ru.iprody.paymentservice.dto.CreateChargeResponseDto;

import java.time.Instant;
import java.util.UUID;

import static ru.iprody.paymentservice.async.XPaymentAdapterStatus.PROCESSING;

@Slf4j
@Component
@AllArgsConstructor
public class PaymentStateCheckHandlerImpl implements PaymentStateCheckHandler {

    private final XPaymentProviderGateway xPaymentProviderGateway;
    private final AsyncSender<XPaymentAdapterResponseMessage> sender;

    @Override
    public boolean handle(UUID chargeGuid) {
        log.info("Checking payment status for chargeGuid: {}", chargeGuid);
        CreateChargeResponseDto response = xPaymentProviderGateway.retrieveCharge(chargeGuid);
        if (response != null) {
            XPaymentAdapterStatus responseStatus = XPaymentAdapterStatus.valueOf(response.getStatus());
            log.info("Payment status={} for chargeGuid={}", responseStatus, chargeGuid);
            if (responseStatus != PROCESSING) {
                XPaymentAdapterResponseMessage responseMessage = new XPaymentAdapterResponseMessage();
                responseMessage.setPaymentGuid(response.getOrder());
                responseMessage.setTransactionRefId(response.getId());
                responseMessage.setAmount(response.getAmount());
                responseMessage.setCurrency(response.getCurrency());
                responseMessage.setStatus(responseStatus);
                responseMessage.setOccurredAt(Instant.now());
                sender.send(responseMessage);
                return true;
            }
        }
        return false;
    }
}