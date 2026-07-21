package com.javier.finance.analytics.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "monthly_finance_snapshots",
        uniqueConstraints = @UniqueConstraint(name = "uk_snapshot_user_month", columnNames = {"user_id", "snapshot_month"}))
public class MonthlyFinanceSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "snapshot_month", nullable = false, length = 7)
    private YearMonth month;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal totalDebt;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal totalSavings;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal retirementBalance;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal monthlyIncome;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal netWorthEstimate;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    void prePersist() { if (createdDate == null) createdDate = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public YearMonth getMonth() { return month; }
    public void setMonth(YearMonth month) { this.month = month; }
    public BigDecimal getTotalDebt() { return totalDebt; }
    public void setTotalDebt(BigDecimal totalDebt) { this.totalDebt = totalDebt; }
    public BigDecimal getTotalSavings() { return totalSavings; }
    public void setTotalSavings(BigDecimal totalSavings) { this.totalSavings = totalSavings; }
    public BigDecimal getRetirementBalance() { return retirementBalance; }
    public void setRetirementBalance(BigDecimal retirementBalance) { this.retirementBalance = retirementBalance; }
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public BigDecimal getNetWorthEstimate() { return netWorthEstimate; }
    public void setNetWorthEstimate(BigDecimal netWorthEstimate) { this.netWorthEstimate = netWorthEstimate; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
