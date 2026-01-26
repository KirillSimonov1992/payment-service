package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.model.Payment;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final Map<Long, Payment> payments = new HashMap<>(5);

    @PostConstruct
    void init() {
        payments.put(1L, new Payment(1L, 1.0));
        payments.put(2L, new Payment(2L, 2.0));
        payments.put(3L, new Payment(3L, 3.0));
        payments.put(4L, new Payment(4L, 4.0));
        payments.put(5L, new Payment(5L, 5.0));
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable Long id) {
        return payments.get(id);
    }

    @GetMapping
    public Map<Long, Payment> getPayments() {
        return payments;
    }

}
