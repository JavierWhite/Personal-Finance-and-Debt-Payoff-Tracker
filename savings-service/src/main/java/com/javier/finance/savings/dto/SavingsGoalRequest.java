package com.javier.finance.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.javier.finance.savings.entity.SavingsStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SavingsGoalRequest(
        @NotNull @Positive Long userId,
        @NotBlank @Size(max = 100) String goalName,
        @NotNull @Positive BigDecimal targetAmount,
        @PositiveOrZero BigDecimal currentAmount,
        LocalDate targetDate,
        SavingsStatus status) {
}
