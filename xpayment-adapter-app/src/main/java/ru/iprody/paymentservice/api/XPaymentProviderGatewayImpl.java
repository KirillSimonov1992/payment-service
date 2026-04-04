package ru.iprody.paymentservice.api;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.iprody.paymentservice.dto.CreateChargeRequestDto;
import ru.iprody.paymentservice.dto.CreateChargeResponseDto;
import ru.iprody.paymentservice.mapper.CreateChargeMapper;
import ru.iprody.xpayment.api.client.DefaultApi;
import ru.iprody.xpayment.api.model.ChargeResponse;
import ru.iprody.xpayment.api.model.CreateChargeRequest;

import java.util.UUID;

@Service
@AllArgsConstructor
public class XPaymentProviderGatewayImpl implements XPaymentProviderGateway {

    private final DefaultApi defaultApi;
    private final CreateChargeMapper chargeMapper;

    @Override
    public CreateChargeResponseDto createCharge(CreateChargeRequestDto createChargeRequestDto) throws RestClientException {
        CreateChargeRequest createChargeRequest = chargeMapper.mapTo(createChargeRequestDto);
        try {
            ChargeResponse chargeResponse = defaultApi.createCharge(createChargeRequest);
            return chargeMapper.mapToDto(chargeResponse);
        } catch (RestClientException e) {
            throw new RestClientException("POST /charges failed", e);
        }
    }

    @Override
    public CreateChargeResponseDto retrieveCharge(UUID id) throws RestClientException {
        try {
            ChargeResponse chargeResponse = defaultApi.retrieveCharge(id);
            return chargeMapper.mapToDto(chargeResponse);
        } catch (Exception e) {
            throw new RestClientException("GET /charges/{id} failed (id=" + id + ")", e);
        }
    }

    // ApiException - не генерируется

//    private RestClientException toRestClientException(String prefix, ApiException e) {
//        String msg = String.format("%s: HTTP %d, body: %s",
//                prefix, e.getCode(),
//                safeStringConverter(e.getResponseBody()));
//        return new RestClientException(msg, e);
//    }
//
//    private String safeStringConverter(String s) {
//        return s == null ? "<empty" : s;
//    }
}