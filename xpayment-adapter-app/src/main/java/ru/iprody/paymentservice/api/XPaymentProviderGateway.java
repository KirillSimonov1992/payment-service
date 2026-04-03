package ru.iprody.paymentservice.api;

import org.springframework.web.client.RestClientException;
import ru.iprody.paymentservice.dto.CreateChargeRequestDto;
import ru.iprody.paymentservice.dto.CreateChargeResponseDto;

import java.util.UUID;

public interface XPaymentProviderGateway {
    CreateChargeResponseDto createCharge(CreateChargeRequestDto createChargeRequestDto) throws RestClientException;
    CreateChargeResponseDto retrieveCharge(UUID id) throws RestClientException;
}