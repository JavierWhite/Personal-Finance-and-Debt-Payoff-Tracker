package com.javier.finance.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SavingsContributionRequest(
        @NotNull @Positive BigDecimal amount,
        LocalDate contributionDate,
        String note) {
}
