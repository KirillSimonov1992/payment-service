package ru.iprody.paymentservice.mapper;

import org.mapstruct.Mapper;
import ru.iprody.paymentservice.dto.CreateChargeRequestDto;
import ru.iprody.paymentservice.dto.CreateChargeResponseDto;
import ru.iprody.xpayment.api.model.ChargeResponse;
import ru.iprody.xpayment.api.model.CreateChargeRequest;

@Mapper(componentModel = "spring")
public interface CreateChargeMapper {
    CreateChargeRequest mapTo(CreateChargeRequestDto dto);
    CreateChargeResponseDto mapToDto(ChargeResponse response);
}