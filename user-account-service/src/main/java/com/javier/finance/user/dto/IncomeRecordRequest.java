package com.javier.finance.user.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.javier.finance.user.entity.IncomeFrequency;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record IncomeRecordRequest(
        @NotNull @Positive Long userId,
        @NotBlank String source,
        @NotNull @DecimalMin("0.01") BigDecimal grossAmount,
        @NotNull IncomeFrequency frequency,
        LocalDate effectiveDate,
        Boolean active) {
}
