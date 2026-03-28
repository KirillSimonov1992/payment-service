package ru.iprody.paymentservice.async.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.iprody.paymentservice.async.AsyncSender;
import ru.iprody.paymentservice.async.XPaymentAdapterRequestMessage;

@Slf4j
@Service
public class KafkaXPaymentAdapterDeadLetterSender
        implements AsyncSender<XPaymentAdapterRequestMessage> {

    private final KafkaTemplate<String, XPaymentAdapterRequestMessage> kafkaTemplate;
    private final String topic;

    public KafkaXPaymentAdapterDeadLetterSender(
            KafkaTemplate<String, XPaymentAdapterRequestMessage> kafkaTemplate,
            @Value("${app.kafka.topics.x-payment-adapter.dlt:x-payment-adapter.dlt}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void send(XPaymentAdapterRequestMessage message) {
        String key = message.getPaymentGuid().toString();
        log.info("Sending XPayment Adapter request in dlt: guid={}, amount={}, currency={} -> topic={}",
                message.getPaymentGuid(), message.getAmount(), message.getCurrency(), topic);
        kafkaTemplate.send(topic, key, message);
    }
}