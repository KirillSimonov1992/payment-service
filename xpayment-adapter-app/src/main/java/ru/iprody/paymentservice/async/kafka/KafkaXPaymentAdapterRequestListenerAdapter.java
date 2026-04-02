package ru.iprody.paymentservice.async.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.iprody.paymentservice.async.AsyncListener;
import ru.iprody.paymentservice.async.MessageHandler;
import ru.iprody.paymentservice.async.XPaymentAdapterRequestMessage;

@Slf4j
@Component
public class KafkaXPaymentAdapterRequestListenerAdapter
    implements AsyncListener<XPaymentAdapterRequestMessage> {

    private final MessageHandler<XPaymentAdapterRequestMessage> handler;

    public KafkaXPaymentAdapterRequestListenerAdapter(MessageHandler<XPaymentAdapterRequestMessage> handler) {
        this.handler = handler;
    }

    @Override
    public void onMessage(XPaymentAdapterRequestMessage message) {
        handler.handleMessage(message);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.x-payment-adapter.request}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            ConsumerRecord<String, XPaymentAdapterRequestMessage> record,
            Acknowledgment ack
    ) {
        XPaymentAdapterRequestMessage message = record.value();
        try {
            log.info("Received XPayment Adapter request: paymentGuid={}, partition {}, offset={}",
                    message.getPaymentGuid(), record.partition(), record.offset());
            onMessage(message);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error handling XPayment Adapter request for paymentGuid={}", message.getPaymentGuid(), e);
            throw e;
        }
    }
}