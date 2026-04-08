package ru.iprody.paymentservice.checkstate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class PaymentStateCheckRegistrarImpl implements PaymentStateCheckRegistrar {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    @Value("${app.rabbitmq.interval-ms:60000}")
    private long intervalMs;

    public PaymentStateCheckRegistrarImpl(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.delayed-exchange-name}") String exchangeName,
            @Value("${app.rabbitmq.queue-name}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @Override
    public void register(UUID chargeGuid, UUID paymentGUID, BigDecimal amount, String currency) {
        PaymentCheckStateMessage message =
                new PaymentCheckStateMessage(
                        chargeGuid, paymentGUID,
                        amount, currency
                );

        log.info("Registering payment state check for chargeGuid - {}", chargeGuid);

        rabbitTemplate.convertAndSend(
                exchangeName,
                routingKey,
                message,
                m -> {
                    m.getMessageProperties().setHeader("x-delayed", intervalMs);
                    m.getMessageProperties().setHeader("x-retry-count", 1);
                    return m;
                }
        );
    }
}