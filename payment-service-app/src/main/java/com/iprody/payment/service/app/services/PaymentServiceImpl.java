package com.iprody.payment.service.app.services;

import com.iprody.payment.service.app.async.AsyncSender;
import com.iprody.payment.service.app.async.XPaymentAdapterRequestMessage;
import com.iprody.payment.service.app.controller.errorhandle.EntityNotFoundException;
import com.iprody.payment.service.app.mapper.PaymentMapper;
import com.iprody.payment.service.app.mapper.XPaymentAdapterMapper;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.persistence.service.dto.CreatePaymentDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentDto;
import com.iprody.payment.service.app.persistency.PaymentFilter;
import com.iprody.payment.service.app.persistency.PaymentFilterFactory;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.iprody.payment.service.app.controller.errorhandle.TypeOperation.DELETE_ENTITY;
import static com.iprody.payment.service.app.controller.errorhandle.TypeOperation.FIND_BY_ID;
import static com.iprody.payment.service.app.controller.errorhandle.TypeOperation.UPDATE_ENTITY;
import static com.iprody.payment.service.app.controller.errorhandle.TypeOperation.UPDATE_STATUS;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_NOT_FOUND = "Payment not found.";
    public static final String CANT_UPDATE_STATUS = "Can't update status. " + PAYMENT_NOT_FOUND;

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final XPaymentAdapterMapper xPaymentAdapterMapper;
    private final AsyncSender<XPaymentAdapterRequestMessage> asyncSender;

    private final Clock clock;

    @Override
    public PaymentDto create(CreatePaymentDto createPaymentDto) {
        final Payment entity = paymentMapper.fromCreateDto(createPaymentDto);
        final Instant now = Instant.now(clock);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        PaymentDto saved = paymentMapper.toDto(
                paymentRepository.save(entity)
        );
        // Отправить сообщение в платежный сервис платежей
        asyncSender.send(xPaymentAdapterMapper.toXPaymentAdapterRequestMessage(saved));
        entity.setNote("Payment has status PROCESSING.");
        return saved;
    }

    @Override
    public PaymentDto findById(UUID guid) {
        return paymentRepository.findById(guid)
                .map(paymentMapper::toDto)
                .orElseThrow(() ->
                        new EntityNotFoundException(PAYMENT_NOT_FOUND, FIND_BY_ID, guid)
                );
    }

    @Override
    @Transactional
    public PaymentDto update(UUID guid, PaymentDto dto) {
        if (!paymentRepository.existsById(guid)) {
            throw new EntityNotFoundException(PAYMENT_NOT_FOUND, UPDATE_ENTITY, guid);
        }

        Payment updated = paymentMapper.toEntity(dto);
        updated.setGuid(guid);
        updated.setUpdatedAt(Instant.now());
        Payment saved = paymentRepository.save(updated);
        return paymentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void updateStatus(UUID guid, PaymentStatus newStatus) {
        if (!paymentRepository.existsById(guid)) {
            throw new EntityNotFoundException(CANT_UPDATE_STATUS, UPDATE_STATUS, guid);
        }
        paymentRepository.updateStatus(guid, newStatus);
    }

    @Override
    @Transactional
    public void updateNote(UUID guid, String newNote) {
        paymentRepository.updateNote(guid, newNote);
    }

    @Override
    public void delete(UUID guid) {
        if (!paymentRepository.existsById(guid)) {
            throw new EntityNotFoundException(PAYMENT_NOT_FOUND, DELETE_ENTITY, guid);
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