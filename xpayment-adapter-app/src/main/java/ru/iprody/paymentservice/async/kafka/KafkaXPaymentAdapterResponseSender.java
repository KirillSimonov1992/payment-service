package ru.iprody.paymentservice.async.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.iprody.paymentservice.async.AsyncSender;
import ru.iprody.paymentservice.async.XPaymentAdapterResponseMessage;

@Slf4j
@Service
public class KafkaXPaymentAdapterResponseSender
    implements AsyncSender<XPaymentAdapterResponseMessage> {

    private final KafkaTemplate<String, XPaymentAdapterResponseMessage> kafkaTemplate;
    private final String topic;

    public KafkaXPaymentAdapterResponseSender(
            KafkaTemplate<String, XPaymentAdapterResponseMessage> kafkaTemplate,
            @Value("${app.kafka.topics.x-payment-adapter.response:x-payment-adapter.responses}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void send(XPaymentAdapterResponseMessage message) {
        String key = message.getPaymentGuid().toString();
        log.info("Sending XPayment Adapter response: guid={}, amount={}, currency={} -> topic={}",
                message.getPaymentGuid(), message.getAmount(), message.getCurrency(), topic);
        kafkaTemplate.send(topic, key, message);
    }
}