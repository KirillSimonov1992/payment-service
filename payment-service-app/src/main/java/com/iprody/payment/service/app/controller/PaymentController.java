package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.persistence.service.dto.CreatePaymentDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentNoteDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentStatusDto;
import com.iprody.payment.service.app.persistency.PaymentFilter;
import com.iprody.payment.service.app.services.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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

@RestController
@AllArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    public static final String DEFAULT_SORT = "desc";
    public static final String DEFAULT_SORT_FIELD = "guid";
    public static final String DEFAULT_PAGE_SIZE = "25";
    public static final String DEFAULT_NUMBER_PAGE = "0";

    private PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto create(@RequestBody CreatePaymentDto paymentDto) {
        return paymentService.create(paymentDto);
    }

    @GetMapping
    public List<PaymentDto> findAll() {
        return paymentService.findAll();
    }

    @GetMapping("/{guid}")
    public PaymentDto get(@PathVariable UUID guid) {
        return paymentService.findById(guid);
    }

    @GetMapping("/search")
    public Page<PaymentDto> search(
            @ModelAttribute PaymentFilter filter,
            @RequestParam(defaultValue = DEFAULT_NUMBER_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_SORT_FIELD) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT) String direction
    ) {
        Sort sort = switch (direction) {
            case "desc" -> Sort.by(sortBy).descending();
            case "asc" -> Sort.by(sortBy).ascending();
            default -> throw new IllegalArgumentException("Invalid direction");
        };
        Pageable pageable = PageRequest.of(page, size, sort);
        return paymentService.search(filter, pageable);
    }

    @PutMapping("/{guid}")
    public PaymentDto update(@PathVariable UUID guid, @RequestBody PaymentDto updateDto) {
        return paymentService.update(guid, updateDto);
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID guid) {
        paymentService.delete(guid);
    }

    @PatchMapping("/{guid}/status")
    public void updateStatus(@PathVariable UUID guid, @RequestBody PaymentStatusDto dto) {
        paymentService.updateStatus(guid, dto.getPaymentStatus());
    }

    @PatchMapping("/{guid}/note")
    public void updateNote(@PathVariable UUID guid, @RequestBody PaymentNoteDto dto) {
        paymentService.updateNote(guid, dto.getNote());
    }
}