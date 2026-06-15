package com.javier.finance.analytics.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record MonthlyFinanceSnapshotRequest(
        @NotNull @Positive Long userId,
        @NotNull YearMonth month,
        @NotNull @PositiveOrZero BigDecimal totalDebt,
        @NotNull @PositiveOrZero BigDecimal totalSavings,
        @NotNull @PositiveOrZero BigDecimal retirementBalance,
        @NotNull @PositiveOrZero BigDecimal monthlyIncome) {
}
