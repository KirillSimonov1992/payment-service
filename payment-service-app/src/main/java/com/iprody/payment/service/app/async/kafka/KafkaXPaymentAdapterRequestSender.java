package com.iprody.payment.service.app.async.kafka;

import com.iprody.payment.service.app.async.AsyncSender;
import com.iprody.payment.service.app.async.XPaymentAdapterRequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaXPaymentAdapterRequestSender
        implements AsyncSender<XPaymentAdapterRequestMessage> {

    private final KafkaTemplate<String, XPaymentAdapterRequestMessage> template;

    private final String topic;

    @Autowired
    public KafkaXPaymentAdapterRequestSender(
            KafkaTemplate<String, XPaymentAdapterRequestMessage> template,
            @Value("${app.kafka.topics.x-payment-adapter.request:xpayment-adapter.requests}") String topic
    ) {
        this.template = template;
        this.topic = topic;
    }

    @Override
    public void send(XPaymentAdapterRequestMessage message) {
        String key = message.getPaymentGuid().toString(); // фиксируем партицирование по платежу
        log.info("Sending XPayment Adapter request: guid={}, amount={}, currency={} -> topic={}",
                message.getPaymentGuid(), message.getAmount(), message.getCurrency(), topic);
        template.send(topic, key, message)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("Error while sending XPayment Adapter request", ex);
                    } else {
                        log.info("XPayment Adapter request sent, partition={}, offset={}",
                                res.getProducerRecord().partition(),
                                res.getRecordMetadata().offset()
                        );
                    }
                });
    }
}