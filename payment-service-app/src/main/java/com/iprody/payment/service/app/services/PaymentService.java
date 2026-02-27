package com.iprody.payment.service.app.services;

import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.persistence.service.dto.CreatePaymentDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentDto;
import com.iprody.payment.service.app.persistency.PaymentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    PaymentDto findById(UUID id);
    Page<PaymentDto> search(PaymentFilter filter, Pageable pageable);
    PaymentDto update(UUID guid, PaymentDto newStatus);
    List<PaymentDto> findAll();
    void updateStatus(UUID guid, PaymentStatus newStatus);
    void updateNote(UUID guid, String newNote);
    void delete(UUID guid);
    PaymentDto create(CreatePaymentDto createPaymentDto);
}
