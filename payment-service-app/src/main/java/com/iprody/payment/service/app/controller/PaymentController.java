package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.persistence.entity.PaymentDto;
import com.iprody.payment.service.app.persistency.PaymentFilter;
import com.iprody.payment.service.app.services.PaymentServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    public static final String DEFAULT_SORT = "desc";
    public static final String DEFAULT_SORT_FIELD = "guid";
    public static final String DEFAULT_PAGE_SIZE = "25";
    public static final String DEFAULT_NUMBER_PAGE = "0";

    private PaymentServiceImpl paymentServiceImpl;

    public PaymentController(PaymentServiceImpl paymentServiceImpl) {
        this.paymentServiceImpl = paymentServiceImpl;
    }

    @GetMapping("/search")
    public Page<PaymentDto> searchPayments(
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
        return paymentServiceImpl.search(filter, pageable);
    }
}
