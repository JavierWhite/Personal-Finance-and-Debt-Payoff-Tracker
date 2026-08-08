
package com.javier.finance.analytics.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DebtPaymentRecordedEvent(
        UUID eventId,
        Long userId,
        Long debtId,
        Long paymentId,
        BigDecimal amount,
        BigDecimal remainingBalance,
        LocalDate paymentDate,
        Instant occurredAt) {
}
