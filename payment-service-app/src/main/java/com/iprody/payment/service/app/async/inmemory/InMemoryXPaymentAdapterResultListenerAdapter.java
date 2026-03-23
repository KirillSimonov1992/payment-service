package com.iprody.payment.service.app.async.inmemory;

import com.iprody.payment.service.app.async.AsyncListener;
import com.iprody.payment.service.app.async.MessageHandler;
import com.iprody.payment.service.app.async.XPaymentAdapterResponseMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
@AllArgsConstructor
public class InMemoryXPaymentAdapterResultListenerAdapter
        implements AsyncListener<XPaymentAdapterResponseMessage> {

    private final MessageHandler<XPaymentAdapterResponseMessage> messageHandler;

    @Override
    public void onMessage(XPaymentAdapterResponseMessage message) {
        log.info("Message received: {}", message);
        messageHandler.handleMessage(message);
    }
}