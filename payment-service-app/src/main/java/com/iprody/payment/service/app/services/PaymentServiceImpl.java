package com.iprody.payment.service.app.services;

import com.iprody.payment.service.app.mapper.PaymentMapper;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.persistence.service.dto.CreatePaymentDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentDto;
import com.iprody.payment.service.app.persistency.PaymentFilter;
import com.iprody.payment.service.app.persistency.PaymentFilterFactory;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDto create(CreatePaymentDto createPaymentDto) {
        final Payment entity = paymentMapper.fromCreateDto(createPaymentDto);
        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return paymentMapper.toDto(
                paymentRepository.save(entity)
        );
    }

    @Override
    public PaymentDto findById(UUID guid) {
        return paymentRepository.findById(guid)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.OK, "Платеж не найден: " + guid));
    }

    @Override
    @Transactional
    public PaymentDto update(UUID guid, PaymentDto dto) {
        if (!paymentRepository.existsById(guid)) {
            throw new EntityNotFoundException("Платеж не найден: " + guid);
        }

        Payment updated = paymentMapper.toEntity(dto);
        updated.setGuid(guid);
        Payment saved = paymentRepository.save(updated);
        return paymentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void updateStatus(UUID guid, PaymentStatus newStatus) {
        paymentRepository.updateStatus(guid, newStatus);
    }

    @Override
    public void updateNote(UUID guid, String newNote) {
        paymentRepository.updateNote(guid, newNote);
    }

    @Override
    public void delete(UUID guid) {
        if (!paymentRepository.existsByGuid(guid)) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Платеж не найден: " + guid);
        }
        paymentRepository.deleteById(guid);
    }

    @Override
    public Page<PaymentDto> search(PaymentFilter filter, Pageable pageable) {
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);
        Page<Payment> page = paymentRepository.findAll(spec, pageable);
        return page.map(paymentMapper::toDto);
    }

    @Override
    public List<PaymentDto> findAll() {
        return paymentRepository.findAll().stream().map(paymentMapper::toDto).toList();
    }


}