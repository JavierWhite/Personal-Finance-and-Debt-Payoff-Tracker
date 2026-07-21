package com.javier.finance.retirement.dto;

import java.math.BigDecimal;

public record RetirementProjectionResponse(
        Long accountId,
        Integer currentAge,
        Integer targetRetirementAge,
        Integer yearsToRetirement,
        BigDecimal currentBalance,
        BigDecimal monthlyContribution,
        BigDecimal expectedAnnualReturn,
        BigDecimal projectedBalance,
        BigDecimal targetRetirementAmount,
        BigDecimal projectedSurplusOrShortfall) {
}
