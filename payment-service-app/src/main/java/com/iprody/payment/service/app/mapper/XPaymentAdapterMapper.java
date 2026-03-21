package com.iprody.payment.service.app.mapper;

import com.iprody.payment.service.app.async.XPaymentAdapterRequestMessage;
import com.iprody.payment.service.app.persistence.service.dto.PaymentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface XPaymentAdapterMapper {
    @Mapping(source = "guid", target = "paymentGuid")
    @Mapping(source = "updatedAt", target = "occurredAt")
    XPaymentAdapterRequestMessage toXPaymentAdapterRequestMessage(PaymentDto paymentDto);
}