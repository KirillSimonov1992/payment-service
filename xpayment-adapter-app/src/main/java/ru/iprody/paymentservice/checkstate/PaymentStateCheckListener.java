package ru.iprody.paymentservice.checkstate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static ru.iprody.paymentservice.checkstate.RabbitMqDlxConfig.DEAD_LETTER_EXCHANGE;
import static ru.iprody.paymentservice.checkstate.RabbitMqDlxConfig.DEAD_LETTER_ROUTING_KEY;

@Slf4j
@Component
public class PaymentStateCheckListener {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;
    private final PaymentStateCheckHandler paymentStateCheckHandler;

    @Value("${app.rabbitmq.max-retries:60}")
    private int maxRetries;

    @Value("${app.rabbitmq.interval-ms:60}")
    private long intervalMs;

    @Autowired
    public PaymentStateCheckListener(
        RabbitTemplate rabbitTemplate,
        @Value("${app.rabbitmq.delayed-exchange-name}") String exchangeName,
        @Value("${app.rabbitmq.queue-name}") String routingKey,
        PaymentStateCheckHandler paymentStateCheckHandler
    ) {
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.paymentStateCheckHandler = paymentStateCheckHandler;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void handle(PaymentCheckStateMessage message, Message raw) {
        log.info("Received payment state check message for chargeGuid={}", message.chargeGuid());
        boolean paid = paymentStateCheckHandler.handle(message.chargeGuid());
        if (paid) {
            return;
        }

        MessageProperties props = raw.getMessageProperties();
        int retryCount = (int) props.getHeaders().getOrDefault("x-retry-count", 0);

        if (retryCount < maxRetries) {
            log.info("Payment isn't paid yet. Retrying in {} ms (attempt {}/{})",
                    intervalMs, retryCount + 1, maxRetries);
            // Планируем следующую проверку
            PaymentCheckStateMessage newMessage = new PaymentCheckStateMessage(
                    message.chargeGuid(),
                    message.paymentGUID(),
                    message.amount(),
                    message.currency()
            );

            rabbitTemplate.convertAndSend(
                    exchangeName,
                    routingKey,
                    newMessage,
                    m -> {
                        m.getMessageProperties().setHeader("x-delay", intervalMs);
                        m.getMessageProperties().setHeader("x-retry-count", retryCount + 1);
                        return m;
                    }
            );
        } else {
            // Исчерпали попытки -- кладём сообщение в DLX
            rabbitTemplate.convertAndSend(
                    DEAD_LETTER_ROUTING_KEY,
                    DEAD_LETTER_EXCHANGE,
                    m -> {
                        m.getMessageProperties().setHeader("x-retry-count", retryCount);
                        m.getMessageProperties().setHeader("x-final-status", "TIMEOUT");
                        m.getMessageProperties().setHeader("x-original-queue", props.getConsumerQueue());
                        return m;
                    }

            );
        }
    }
}