package ru.iprody.paymentservice.async;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import ru.iprody.paymentservice.api.XPaymentProviderGateway;
import ru.iprody.paymentservice.checkstate.PaymentStateCheckRegistrar;
import ru.iprody.paymentservice.dto.CreateChargeRequestDto;
import ru.iprody.paymentservice.dto.CreateChargeResponseDto;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Component
@AllArgsConstructor
public class RequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private final AsyncSender<XPaymentAdapterResponseMessage> sender;
    private final XPaymentProviderGateway xPaymentProviderGateway;
    private final PaymentStateCheckRegistrar paymentStateCheckRegistrar;

    @Override
    public void handleMessage(XPaymentAdapterRequestMessage message) {
        log.info(
                "Payment request received paymentGuid - {},amount - {}, currency - {}",
                message.getPaymentGuid(), message.getAmount(), message.getCurrency()
        );

        CreateChargeRequestDto createChargeRequest = new CreateChargeRequestDto();
        createChargeRequest.setAmount(message.getAmount());
        createChargeRequest.setCurrency(message.getCurrency());
        createChargeRequest.setOrder(message.getPaymentGuid());
        XPaymentAdapterResponseMessage responseMessage = new XPaymentAdapterResponseMessage();
        try {
            CreateChargeResponseDto chargeResponseDto = xPaymentProviderGateway.createCharge(createChargeRequest);
            log.info("Payment request with paymentGuid - {} is sent for payment processing." +
                    "Current status: {}", chargeResponseDto.getStatus());

            responseMessage.setPaymentGuid(chargeResponseDto.getOrder());
            responseMessage.setTransactionRefId(chargeResponseDto.getId());
            responseMessage.setAmount(chargeResponseDto.getAmount());
            responseMessage.setCurrency(chargeResponseDto.getCurrency());
            responseMessage.setStatus(XPaymentAdapterStatus.valueOf(chargeResponseDto.getStatus()));
            responseMessage.setOccurredAt(Instant.now());
            sender.send(responseMessage);
            paymentStateCheckRegistrar.register(
                    chargeResponseDto.getId(),
                    chargeResponseDto.getOrder(),
                    chargeResponseDto.getAmount(),
                    chargeResponseDto.getCurrency()
            );
        } catch (RestClientException e) {
            log.error("Error in time of sending payment request with paymentGuid - {}", message.getPaymentGuid(), e);

            responseMessage.setPaymentGuid(message.getPaymentGuid());
            responseMessage.setAmount(message.getAmount());
            responseMessage.setCurrency(message.getCurrency());
            responseMessage.setStatus(XPaymentAdapterStatus.CANCELED);
            responseMessage.setOccurredAt(Instant.now());

        }

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