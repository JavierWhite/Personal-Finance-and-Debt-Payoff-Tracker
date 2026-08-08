
package com.javier.finance.debt.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class DebtPaymentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(DebtPaymentEventPublisher.class);

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final String queueName;

    public DebtPaymentEventPublisher(
            JmsTemplate jmsTemplate,
            ObjectMapper objectMapper,
            @Value("${finance.messaging.debt-payment-queue:finance.debt.payment.recorded}") String queueName) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
    }

    public void publish(DebtPaymentRecordedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            jmsTemplate.convertAndSend(queueName, payload, message -> {
                message.setStringProperty("eventType", "DebtPaymentRecorded");
                message.setStringProperty("eventId", event.eventId().toString());
                message.setStringProperty("userId", event.userId().toString());
                return message;
            });
            log.info("Published debt payment JMS event eventId={} debtId={} paymentId={} queue={}",
                    event.eventId(), event.debtId(), event.paymentId(), queueName);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize debt payment JMS event", exception);
        }
    }
}
