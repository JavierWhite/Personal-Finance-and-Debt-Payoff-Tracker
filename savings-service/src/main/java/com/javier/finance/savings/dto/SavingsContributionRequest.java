package com.javier.finance.savings.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SavingsContributionRequest(
        @NotNull @Positive BigDecimal amount,
        LocalDate contributionDate,
        @Size(max = 250) String note) {
}
