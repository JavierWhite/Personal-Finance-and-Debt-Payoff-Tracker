package com.javier.finance.debt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DebtPaymentRequest(
        @NotNull @Positive BigDecimal amount,
        LocalDate paymentDate,
        @Size(max = 250) String note) {
}
