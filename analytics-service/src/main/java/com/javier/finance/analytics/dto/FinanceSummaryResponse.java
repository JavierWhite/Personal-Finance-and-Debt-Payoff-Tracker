package com.javier.finance.analytics.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record FinanceSummaryResponse(
        Long userId,
        YearMonth month,
        BigDecimal totalDebt,
        BigDecimal totalSavings,
        BigDecimal retirementBalance,
        BigDecimal monthlyIncome,
        BigDecimal netWorthEstimate) {
}
