package com.javier.finance.retirement.dto;

import java.math.BigDecimal;

import com.javier.finance.retirement.entity.RetirementAccountType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record RetirementAccountRequest(
        @NotNull @Positive Long userId,
        @NotBlank String accountName,
        @NotNull RetirementAccountType accountType,
        @NotNull @PositiveOrZero BigDecimal currentBalance,
        @NotNull @PositiveOrZero BigDecimal monthlyContribution,
        @NotNull @PositiveOrZero BigDecimal expectedAnnualReturn,
        @NotNull @Positive BigDecimal targetRetirementAmount,
        @NotNull @Min(18) @Max(100) Integer currentAge,
        @NotNull @Min(19) @Max(100) Integer targetRetirementAge) {
}
