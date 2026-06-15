package com.javier.finance.debt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.javier.finance.debt.entity.DebtStatus;
import com.javier.finance.debt.entity.DebtType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record DebtAccountRequest(
        @NotNull @Positive Long userId,
        @NotBlank String debtName,
        @NotNull DebtType debtType,
        @NotNull @DecimalMin("0.01") BigDecimal originalBalance,
        @PositiveOrZero BigDecimal currentBalance,
        @NotNull @PositiveOrZero BigDecimal interestRate,
        @NotNull @PositiveOrZero BigDecimal minimumPayment,
        LocalDate dueDate,
        DebtStatus status) {
}
