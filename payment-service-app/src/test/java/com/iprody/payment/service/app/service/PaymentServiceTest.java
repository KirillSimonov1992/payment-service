package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.controller.errorhandle.EntityNotFoundException;
import com.iprody.payment.service.app.mapper.PaymentMapper;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.service.dto.CreatePaymentDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.persistency.PaymentFilter;
import com.iprody.payment.service.app.persistency.PaymentFilterFactory;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import com.iprody.payment.service.app.services.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.iprody.payment.service.app.controller.PaymentController.DEFAULT_NUMBER_PAGE;
import static com.iprody.payment.service.app.controller.PaymentController.DEFAULT_PAGE_SIZE;
import static com.iprody.payment.service.app.controller.PaymentController.DEFAULT_SORT_FIELD;
import static com.iprody.payment.service.app.controller.errorhandle.TypeOperation.DELETE_ENTITY;
import static com.iprody.payment.service.app.controller.errorhandle.TypeOperation.UPDATE_ENTITY;
import static com.iprody.payment.service.app.controller.errorhandle.TypeOperation.UPDATE_STATUS;
import static com.iprody.payment.service.app.services.PaymentServiceImpl.CANT_UPDATE_STATUS;
import static com.iprody.payment.service.app.services.PaymentServiceImpl.PAYMENT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class PaymentServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-02-27T10:00:00Z");

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentDto paymentDto;
    private UUID guid;

    @BeforeEach
    void setUp() {
        guid = UUID.randomUUID();
        payment = new Payment();
        payment.setGuid(guid);
        payment.setInquiryRefId(UUID.randomUUID());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setCreatedAt(FIXED_INSTANT);
        payment.setUpdatedAt(FIXED_INSTANT);
        payment.setNote("note test");

        paymentDto = new PaymentDto();
        paymentDto.setGuid(payment.getGuid());
        paymentDto.setInquiryRefId(payment.getInquiryRefId());
        paymentDto.setAmount(payment.getAmount());
        paymentDto.setCurrency(payment.getCurrency());
        paymentDto.setStatus(payment.getStatus());
        paymentDto.setCreatedAt(payment.getCreatedAt());
        paymentDto.setUpdatedAt(payment.getUpdatedAt());
        paymentDto.setUpdatedAt(payment.getUpdatedAt());
        paymentDto.setNote(payment.getNote());
    }

    @ParameterizedTest
    @MethodSource("statusProvider")
    void shouldMapDifferentPaymentStatuses(PaymentStatus status) {
        // given
        payment.setStatus(status);
        paymentDto.setStatus(status);

        when(paymentRepository.findById(guid)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        // when
        PaymentDto result = paymentService.findById(guid);

        // then
        assertEquals(status, result.getStatus());

        verify(paymentRepository).findById(guid);
        verify(paymentMapper).toDto(payment);
    }

    static PaymentStatus[] statusProvider() {
        return new PaymentStatus[]{
                PaymentStatus.RECEIVED,
                PaymentStatus.PENDING,
                PaymentStatus.APPROVED,
                PaymentStatus.DECLINED,
                PaymentStatus.NOT_SENT,
        };
    }

    @Test
    void shouldReturnPaymentDtoById() {
        // given

        when(paymentRepository.findById(payment.getGuid())).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        // when
        PaymentDto result = paymentService.findById(payment.getGuid());

        // then
        assertNotEquals(null, result);
        assertEquals(payment.getGuid(), result.getGuid());
        assertEquals(payment.getCurrency(), result.getCurrency());
        assertEquals(payment.getAmount(), result.getAmount());
        assertEquals(payment.getCreatedAt(), result.getCreatedAt());
        assertEquals(payment.getUpdatedAt(), result.getUpdatedAt());
        assertEquals(payment.getTransactionRefId(), result.getTransactionRefId());
    }

    @Test
    void shouldReturnPaymentDtoByCurrency() {
        // given
        PaymentFilter filter = new PaymentFilter();
        filter.setCurrency("USD");
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);

        Pageable pageableRequest = PageRequest.of(0, 10);

        Pageable pageableResponse = PageRequest.of(0, 10);
        Page<Payment> pageDtoResponse = new PageImpl<>(Collections.singletonList(payment), pageableResponse, 1);

        try (MockedStatic mocked = mockStatic(PaymentFilterFactory.class)) {

            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);

            when(paymentRepository.findAll(spec, pageableRequest)).thenReturn(pageDtoResponse);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            Page<PaymentDto> results = paymentService.search(filter, pageableRequest);

            // then
            assertNotEquals(null, results);
            assertNotEquals(null, results);
            assertEquals(1, results.getContent().size());
            PaymentDto dtoFromResults = results.getContent().getFirst();
            assertEquals(dtoFromResults.getCurrency(), payment.getCurrency());
        }
    }

    @Test
    void shouldReturnPaymentDtoByMinAmount() {
        // given
        PaymentFilter filter = new PaymentFilter();
        filter.setMinAmount(payment.getAmount().subtract(new BigDecimal(100)));
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);

        Pageable pageableRequest = PageRequest.of(0, 10);

        Pageable pageableResponse = PageRequest.of(0, 10);
        Page<Payment> pageDtoResponse = new PageImpl<>(Collections.singletonList(payment), pageableResponse, 1);

        try (MockedStatic mocked = mockStatic(PaymentFilterFactory.class)) {

            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);

            when(paymentRepository.findAll(spec, pageableRequest)).thenReturn(pageDtoResponse);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            Page<PaymentDto> results = paymentService.search(filter, pageableRequest);

            // then
            assertNotEquals(null, results);
            assertFalse(results.getContent().isEmpty());
            assertEquals(1, results.getContent().size());
            PaymentDto dtoFromResults = results.getContent().getFirst();
            assertTrue(dtoFromResults.getAmount().compareTo(filter.getMinAmount()) > 0, "Значение должно быть больше");
        }
    }

    @Test
    void shouldReturnPaymentDtoByMaxAmount() {
        // given
        PaymentFilter filter = new PaymentFilter();
        BigDecimal max = payment.getAmount().add(new BigDecimal(100));
        filter.setMaxAmount(max);
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);

        Pageable pageableRequest = PageRequest.of(0, 10);

        Pageable pageableResponse = PageRequest.of(0, 10);
        Page<Payment> pageDtoResponse = new PageImpl<>(Collections.singletonList(payment), pageableResponse, 1);

        try (MockedStatic mocked = mockStatic(PaymentFilterFactory.class)) {

            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);

            when(paymentRepository.findAll(spec, pageableRequest)).thenReturn(pageDtoResponse);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            Page<PaymentDto> results = paymentService.search(filter, pageableRequest);

            // then
            assertNotNull(results);
            assertFalse(results.getContent().isEmpty());
            assertEquals(1, results.getContent().size());

            PaymentDto dtoFromResults = results.getContent().getFirst();
            assertTrue(dtoFromResults.getAmount().compareTo(filter.getMaxAmount()) < 0);
        }
    }

    @Test
    void shouldReturnPaymentDtoByCreatedAfter() {
        // given
        PaymentFilter filter = new PaymentFilter();
        filter.setCreatedAfter(payment.getCreatedAt().minus(1, ChronoUnit.HOURS));
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);

        Pageable pageableRequest = PageRequest.of(0, 10);

        Pageable pageableResponse = PageRequest.of(0, 10);
        Page<Payment> pageDtoResponse = new PageImpl<>(Collections.singletonList(payment), pageableResponse, 1);

        try (MockedStatic mocked = mockStatic(PaymentFilterFactory.class)) {

            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);

            when(paymentRepository.findAll(spec, pageableRequest)).thenReturn(pageDtoResponse);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            Page<PaymentDto> results = paymentService.search(filter, pageableRequest);

            // then
            assertNotNull(results);
            assertFalse(results.getContent().isEmpty());
            assertEquals(1, results.getContent().size());
            PaymentDto dtoFromResults = results.getContent().getFirst();
            assertTrue(dtoFromResults.getCreatedAt().isAfter(filter.getCreatedAfter()));
        }
    }

    @Test
    void shouldReturnPaymentDtoByCreatedBefore() {
        // given
        PaymentFilter filter = new PaymentFilter();
        filter.setCreatedBefore(payment.getCreatedAt().plus(1, ChronoUnit.HOURS));
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);

        Pageable pageableRequest = PageRequest.of(0, 10);

        Pageable pageableResponse = PageRequest.of(0, 10);
        Page<Payment> pageDtoResponse = new PageImpl<>(Collections.singletonList(payment), pageableResponse, 1);

        try (MockedStatic mocked = mockStatic(PaymentFilterFactory.class)) {

            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);

            when(paymentRepository.findAll(spec, pageableRequest)).thenReturn(pageDtoResponse);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            Page<PaymentDto> results = paymentService.search(filter, pageableRequest);

            // then
            assertNotNull(results);
            assertFalse(results.getContent().isEmpty());
            assertEquals(1, results.getContent().size());
            PaymentDto dtoFromResults = results.getContent().getFirst();
            assertTrue(dtoFromResults.getCreatedAt().isBefore(filter.getCreatedBefore()));
        }
    }

    @Test
    void shouldReturnPaymentDtoByCurrencyAndStatus() {
        // given
        PaymentFilter filter = new PaymentFilter();
        filter.setCurrency(payment.getCurrency());
        filter.setStatus(payment.getStatus());
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);

        Pageable pageableRequest = PageRequest.of(0, 10);

        Pageable pageableResponse = PageRequest.of(0, 10);
        Page<Payment> pageDtoResponse = new PageImpl<>(Collections.singletonList(payment), pageableResponse, 1);

        try (MockedStatic mocked = mockStatic(PaymentFilterFactory.class)) {

            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);

            when(paymentRepository.findAll(spec, pageableRequest)).thenReturn(pageDtoResponse);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            Page<PaymentDto> results = paymentService.search(filter, pageableRequest);

            // then
            assertNotNull(results);
            assertNotEquals(0, results.getNumberOfElements());
            assertEquals(1, results.getContent().size());
            PaymentDto dtoFromResults = results.getContent().getFirst();
            assertEquals(dtoFromResults.getStatus(), filter.getStatus());
            assertEquals(dtoFromResults.getCurrency(), filter.getCurrency());
        }
    }

    @Test
    void shouldReturnPageWithSortIdAndSize() {
        // given
        PaymentFilter filter = new PaymentFilter();
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);

        Sort defaultSort = Sort.by(DEFAULT_SORT_FIELD).descending();
        Pageable pageableRequest =
                PageRequest.of(
                        Integer.parseInt(DEFAULT_NUMBER_PAGE),
                        Integer.parseInt(DEFAULT_PAGE_SIZE),
                        defaultSort
                );

        Pageable pageableResponse =
                PageRequest.of(
                        Integer.parseInt(DEFAULT_NUMBER_PAGE),
                        Integer.parseInt(DEFAULT_PAGE_SIZE),
                        defaultSort
                );
        Page<Payment> pageDtoResponse = new PageImpl<>(Collections.singletonList(payment), pageableResponse, 1);

        try (MockedStatic mocked = mockStatic(PaymentFilterFactory.class)) {

            mocked.when(() -> PaymentFilterFactory.fromFilter(filter)).thenReturn(spec);

            when(paymentRepository.findAll(spec, pageableRequest)).thenReturn(pageDtoResponse);
            when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

            // when
            Page<PaymentDto> results = paymentService.search(filter, pageableRequest);

            // then
            assertNotNull(results);
            assertNotEquals(0, results.getNumberOfElements());
            assertEquals(0, results.getNumber());
            assertEquals(25, results.getSize());
            assertEquals(defaultSort, results.getSort());
        }
    }

    @Test
    void shouldReturnCreated() {
        // given

        CreatePaymentDto createPaymentDto =
                new CreatePaymentDto(
                        payment.getInquiryRefId(),
                        payment.getAmount(),
                        payment.getCurrency(),
                        payment.getTransactionRefId(),
                        payment.getStatus(),
                        payment.getNote()
                );

        when(paymentMapper.fromCreateDto(createPaymentDto)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);
        when(paymentRepository.save(payment)).thenReturn(payment);

        // when
        PaymentDto result = paymentService.create(createPaymentDto);

        // then
        assertNotNull(result);
        assertNotNull(result.getGuid());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(createPaymentDto.note(), result.getNote());
        assertEquals(createPaymentDto.currency(), result.getCurrency());
        assertEquals(createPaymentDto.inquiryRefId(), result.getInquiryRefId());
        assertEquals(createPaymentDto.transactionRefId(), result.getTransactionRefId());
        assertEquals(createPaymentDto.status(), result.getStatus());
    }

    @Test
    void shouldReturnUpdated() {
        // given
        final UUID guid = payment.getGuid();

        PaymentDto dtoForUpdate = new PaymentDto();
        dtoForUpdate.setGuid(guid);
        dtoForUpdate.setInquiryRefId(guid);
        dtoForUpdate.setAmount(payment.getAmount());
        dtoForUpdate.setCurrency(payment.getCurrency());
        dtoForUpdate.setTransactionRefId(payment.getTransactionRefId());
        dtoForUpdate.setStatus(payment.getStatus());
        dtoForUpdate.setNote(payment.getNote());
        dtoForUpdate.setCreatedAt(FIXED_INSTANT);
        dtoForUpdate.setUpdatedAt(FIXED_INSTANT);

        when(paymentRepository.existsById(guid)).thenReturn(true);
        when(paymentMapper.toEntity(dtoForUpdate)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(dtoForUpdate);

        // when
        PaymentDto result = paymentService.update(guid, dtoForUpdate);

        // then
        assertNotNull(result);
        assertNotNull(result.getGuid());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals(dtoForUpdate.getNote(), result.getNote());
        assertEquals(dtoForUpdate.getCurrency(), result.getCurrency());
        assertEquals(dtoForUpdate.getInquiryRefId(), result.getInquiryRefId());
        assertEquals(dtoForUpdate.getTransactionRefId(), result.getTransactionRefId());
    }

    @Test
    void shouldReturnUpdateEntityNotFound() {
        // given
        final UUID notExistsGuid = UUID.randomUUID();

        PaymentDto dtoForUpdate = new PaymentDto();

        when(paymentRepository.existsById(notExistsGuid)).thenReturn(false);

        // when
        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> {
                    paymentService.update(notExistsGuid, dtoForUpdate);
                }
        );

        // then
        assertEquals(PAYMENT_NOT_FOUND, thrown.getMessage());
        assertEquals(notExistsGuid, thrown.getEntityId());
        assertEquals(UPDATE_ENTITY, thrown.getOperation());
    }

    @Test
    void shouldReturnWhenUpdateStatusEntityNotFound() {
        // given
        final UUID notExistsGuid = UUID.randomUUID();

        when(paymentRepository.existsById(notExistsGuid)).thenReturn(false);

        // when
        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> {
                    paymentService.updateStatus(notExistsGuid, PaymentStatus.NOT_SENT);
                }
        );

        // then
        assertEquals(CANT_UPDATE_STATUS, thrown.getMessage());
        assertEquals(notExistsGuid, thrown.getEntityId());
        assertEquals(UPDATE_STATUS, thrown.getOperation());
    }

    @Test
    void shouldReturnWhenDeleteEntityNotFound() {
        // given
        final UUID notExistsGuid = UUID.randomUUID();

        when(paymentRepository.existsById(notExistsGuid)).thenReturn(false);

        // when
        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> {
                    paymentService.delete(notExistsGuid);
                }
        );

        // then
        assertEquals(PAYMENT_NOT_FOUND, thrown.getMessage());
        assertEquals(notExistsGuid, thrown.getEntityId());
        assertEquals(DELETE_ENTITY, thrown.getOperation());
    }
}