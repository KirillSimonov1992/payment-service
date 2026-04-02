package com.iprody.payment.service.app.services;

import com.iprody.payment.service.app.async.MessageHandler;
import com.iprody.payment.service.app.async.XPaymentAdapterResponseMessage;
import com.iprody.payment.service.app.async.XPaymentAdapterStatus;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class XPaymentAdapterMessageHandler implements MessageHandler<XPaymentAdapterResponseMessage> {

    private PaymentRepository paymentRepo;

    @Override
    public void handleMessage(XPaymentAdapterResponseMessage message) {
        log.info("Received message with id {} and state {}", message.getMessageGuid(), message.getStatus());
        Payment p = paymentRepo.findById(message.getPaymentGuid()).orElseThrow();
        p.setTransactionRefId(message.getTransactionRefId());
        if (message.getStatus() == XPaymentAdapterStatus.SUCCEEDED) {
            p.setStatus(PaymentStatus.APPROVED);
        } else {
            p.setStatus(PaymentStatus.DECLINED);
        }
        paymentRepo.save(p);
        log.debug("handleMessage saved payment {}", p);
    }
}