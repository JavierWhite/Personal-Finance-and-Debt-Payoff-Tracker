package com.javier.finance.debt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DebtPaymentRequest(
        @NotNull @Positive BigDecimal amount,
        LocalDate paymentDate,
        String note) {
}
