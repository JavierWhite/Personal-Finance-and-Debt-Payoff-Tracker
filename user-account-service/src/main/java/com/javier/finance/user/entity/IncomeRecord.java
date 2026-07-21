package com.javier.finance.user.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "income_records")
public class IncomeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 120)
    private String source;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal grossAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncomeFrequency frequency;

    private LocalDate effectiveDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    void prePersist() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    @Transient
    public BigDecimal getMonthlyGrossAmount() {
        if (grossAmount == null || frequency == null) {
            return BigDecimal.ZERO;
        }
        return frequency == IncomeFrequency.MONTHLY
                ? grossAmount.setScale(2, RoundingMode.HALF_UP)
                : grossAmount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getYearlyGrossAmount() {
        if (grossAmount == null || frequency == null) {
            return BigDecimal.ZERO;
        }
        return frequency == IncomeFrequency.YEARLY
                ? grossAmount.setScale(2, RoundingMode.HALF_UP)
                : grossAmount.multiply(BigDecimal.valueOf(12)).setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public IncomeFrequency getFrequency() { return frequency; }
    public void setFrequency(IncomeFrequency frequency) { this.frequency = frequency; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
