package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/{guid}")
    public Payment getPayment(@PathVariable UUID guid) {
        return paymentRepository.findById(guid).orElseThrow();
    }

    @GetMapping
    public List<Payment> getPayments() {
        return paymentRepository.findAll();
    }

}
