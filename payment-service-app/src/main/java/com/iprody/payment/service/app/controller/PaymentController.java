package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.persistence.service.dto.CreatePaymentDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentNoteDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentStatusDto;
import com.iprody.payment.service.app.persistency.PaymentFilter;
import com.iprody.payment.service.app.services.PaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    public static final String DEFAULT_SORT = "desc";
    public static final String DEFAULT_SORT_FIELD = "guid";
    public static final String DEFAULT_PAGE_SIZE = "25";
    public static final String DEFAULT_NUMBER_PAGE = "0";

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('admin')")
    public PaymentDto create(@RequestBody CreatePaymentDto paymentDto) {
        log.info("POST create by paymentDto {}", paymentDto);
        PaymentDto response = paymentService.create(paymentDto);
        log.debug("POST create by paymentDto. Sending response PaymentDto: {}", response);
        return response;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public List<PaymentDto> findAll() {
        log.info("GET findAll");
        List<PaymentDto> response = paymentService.findAll();
        log.debug("GET findAll. Sending response List<PaymentDto>: {}", response);
        return response;
    }

    @GetMapping("/{guid}")
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public PaymentDto get(@PathVariable UUID guid) {
        log.info("GET /{guid} guid {}", guid);
        PaymentDto response = paymentService.findById(guid);
        log.debug("GET /{guid}. Sending response List<PaymentDto>: {}", response);
        return response;
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('admin', 'reader')")
    public Page<PaymentDto> search(
            @ModelAttribute PaymentFilter filter,
            @RequestParam(defaultValue = DEFAULT_NUMBER_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_FIELD) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT) String direction
    ) {
        log.info("GET /search page {}, size {}", page, size);
        Sort sort = switch (direction) {
            case "desc" -> Sort.by(sortBy).descending();
            case "asc" -> Sort.by(sortBy).ascending();
            default -> throw new IllegalArgumentException("Invalid direction");
        };
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PaymentDto> response = paymentService.search(filter, pageable);
        log.debug("GET /search. Sending response Page<PaymentDto>: {}", response);
        return response;
    }

    @PutMapping("/{guid}")
    @PreAuthorize("hasRole('admin')")
    public PaymentDto update(@PathVariable UUID guid, @RequestBody PaymentDto updateDto) {
        log.info("PUT /{guid} update guid {}, updateDto {}", guid, updateDto);
        PaymentDto response = paymentService.update(guid, updateDto);
        log.debug("PUT /{guid}. Sending response Page<PaymentDto>: {}", response);
        return response;
    }

    @DeleteMapping("/{guid}")
    @PreAuthorize("hasRole('admin')")
    public void delete(@PathVariable UUID guid) {
        log.info("DELETE /{guid} guid {}", guid);
        paymentService.delete(guid);
        log.debug("DELETE /{guid}. Payment deleted with guid {}", guid);
    }

    @PatchMapping("/{guid}/status")
    @PreAuthorize("hasRole('admin')")
    public void updateStatus(@PathVariable UUID guid, @RequestBody PaymentStatusDto dto) {
        log.info("PATCH /{guid}/status update status guid {}, dto {}", guid, dto);
        paymentService.updateStatus(guid, dto.getPaymentStatus());
        log.debug("PATCH /{guid}/status. Payment updated status with guid {}", guid);
    }

    @PatchMapping("/{guid}/note")
    @PreAuthorize("hasRole('admin')")
    public void updateNote(@PathVariable UUID guid, @RequestBody PaymentNoteDto dto) {
        log.info("PATCH /{guid}/note update note guid {}, dto {}", guid, dto);
        paymentService.updateNote(guid, dto.getNote());
        log.debug("PATCH /{guid}/note. Payment updated note with guid {}", guid);
    }
}