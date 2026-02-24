package com.iprody.payment.service.app.mapper;

import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentDto;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentMapperTest {

    private final PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

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
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        // when
        PaymentDto dto = mapper.toDto(payment);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getGuid()).isEqualTo(payment.getGuid());
        assertThat(dto.getAmount()).isEqualTo(payment.getAmount());
        assertThat(dto.getCurrency()).isEqualTo(payment.getCurrency());
        assertThat(dto.getInquiryRefId()).isEqualTo(payment.getInquiryRefId());
        assertThat(dto.getStatus()).isEqualTo(payment.getStatus());
        assertThat(dto.getCreatedAt()).isEqualTo(payment.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(payment.getUpdatedAt());
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
        assertThat(entity).isNotNull();
        assertThat(entity.getGuid()).isEqualTo(dto.getGuid());
        assertThat(entity.getAmount()).isEqualTo(dto.getAmount());
        assertThat(entity.getCurrency()).isEqualTo(dto.getCurrency());
        assertThat(entity.getInquiryRefId()).isEqualTo(dto.getInquiryRefId());
        assertThat(entity.getStatus()).isEqualTo(dto.getStatus());
        assertThat(entity.getCreatedAt()).isEqualTo(dto.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(dto.getUpdatedAt());

    }
}
