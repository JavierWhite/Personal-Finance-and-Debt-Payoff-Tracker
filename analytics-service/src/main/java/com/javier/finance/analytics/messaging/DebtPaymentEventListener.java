
package com.javier.finance.analytics.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javier.finance.analytics.entity.ProcessedDebtPaymentEvent;
import com.javier.finance.analytics.repository.ProcessedDebtPaymentEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DebtPaymentEventListener {
    private static final Logger log = LoggerFactory.getLogger(DebtPaymentEventListener.class);

    private final ObjectMapper objectMapper;
    private final ProcessedDebtPaymentEventRepository repository;

    public DebtPaymentEventListener(ObjectMapper objectMapper, ProcessedDebtPaymentEventRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @JmsListener(destination = "${finance.messaging.debt-payment-queue:finance.debt.payment.recorded}")
    @Transactional
    public void receive(String payload) {
        DebtPaymentRecordedEvent event;
        try {
            event = objectMapper.readValue(payload, DebtPaymentRecordedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize debt payment JMS event", exception);
        }

        String eventId = event.eventId().toString();
        if (repository.existsByEventId(eventId)) {
            log.info("Ignoring duplicate debt payment JMS event eventId={}", eventId);
            return;
        }

        ProcessedDebtPaymentEvent processed = new ProcessedDebtPaymentEvent();
        processed.setEventId(eventId);
        processed.setUserId(event.userId());
        processed.setDebtId(event.debtId());
        processed.setPaymentId(event.paymentId());
        processed.setAmount(event.amount());
        processed.setRemainingBalance(event.remainingBalance());
        processed.setPaymentDate(event.paymentDate());
        processed.setOccurredAt(event.occurredAt());
        repository.save(processed);

        log.info("Processed debt payment JMS event eventId={} userId={} debtId={} paymentId={}",
                eventId, event.userId(), event.debtId(), event.paymentId());
    }
}
