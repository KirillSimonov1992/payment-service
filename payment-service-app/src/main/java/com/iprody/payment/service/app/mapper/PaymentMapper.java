package com.iprody.payment.service.app.mapper;

import com.iprody.payment.service.app.persistence.service.dto.CreatePaymentDto;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.service.dto.PaymentDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto toDto(Payment payment);
    Payment toEntity(PaymentDto paymentDto);
    Payment fromCreateDto(CreatePaymentDto createPaymentDto);
}