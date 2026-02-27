package com.iprody.payment.service.app.mapper;

import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentDto;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PaymentMapperTest {

    private final PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);
    private final Instant date = Instant.parse("2026-02-27T10:00:00Z");

    @Test
    void shouldMapToDto() {
        // given
        UUID guid = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setGuid(guid);
        payment.setAmount(new BigDecimal("123.45"));
        payment.setCurrency("USD");
        payment.setInquiryRefId(UUID.randomUUID());
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setCreatedAt(date);
        payment.setUpdatedAt(date);

        // when
        PaymentDto dto = mapper.toDto(payment);

        // then
        assertNotNull(dto);
        assertEquals(payment.getGuid(), dto.getGuid());
        assertEquals(payment.getAmount(), dto.getAmount());
        assertEquals(payment.getCurrency(), dto.getCurrency());
        assertEquals(payment.getInquiryRefId(), dto.getInquiryRefId());
        assertEquals(payment.getStatus(), dto.getStatus());
        assertEquals(payment.getCreatedAt(), dto.getCreatedAt());
        assertEquals(payment.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    void shouldMapToEntity() {
        // given
        UUID guid = UUID.randomUUID();
        Instant now = Instant.now();

        PaymentDto dto = new PaymentDto(
                guid,
                guid,
                new BigDecimal("999.99"),
                "EUR",
                guid,
                PaymentStatus.PENDING,
                "note",
                now,
                now
        );

        // when
        Payment entity = mapper.toEntity(dto);

        //then
        assertNotNull(entity);
        assertEquals(dto.getGuid(), entity.getGuid());
        assertEquals(dto.getAmount(), entity.getAmount());
        assertEquals(dto.getCurrency(), entity.getCurrency());
        assertEquals(dto.getInquiryRefId(), entity.getInquiryRefId());
        assertEquals(dto.getStatus(), entity.getStatus());
        assertEquals(dto.getCreatedAt(), entity.getCreatedAt());
        assertEquals(dto.getUpdatedAt(), entity.getUpdatedAt());

    }
}
