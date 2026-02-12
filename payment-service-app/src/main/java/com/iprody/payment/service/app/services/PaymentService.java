package com.iprody.payment.service.app.services;

import com.iprody.payment.service.app.persistence.entity.PaymentDto;
import com.iprody.payment.service.app.persistency.PaymentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentService {
    PaymentDto get(UUID id);
    Page<PaymentDto> search(PaymentFilter filter, Pageable pageable);
}
