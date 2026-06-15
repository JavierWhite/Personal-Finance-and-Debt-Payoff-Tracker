package com.javier.finance.retirement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "retirement_accounts")
public class RetirementAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false, length = 120)
    private String accountName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RetirementAccountType accountType;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal currentBalance;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyContribution;
    @Column(nullable = false, precision = 7, scale = 3)
    private BigDecimal expectedAnnualReturn;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal targetRetirementAmount;
    @Column(nullable = false)
    private Integer currentAge;
    @Column(nullable = false)
    private Integer targetRetirementAge;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "retirementAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RetirementContribution> contributions = new ArrayList<>();

    @PrePersist
    void prePersist() { if (createdDate == null) createdDate = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public RetirementAccountType getAccountType() { return accountType; }
    public void setAccountType(RetirementAccountType accountType) { this.accountType = accountType; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public BigDecimal getMonthlyContribution() { return monthlyContribution; }
    public void setMonthlyContribution(BigDecimal monthlyContribution) { this.monthlyContribution = monthlyContribution; }
    public BigDecimal getExpectedAnnualReturn() { return expectedAnnualReturn; }
    public void setExpectedAnnualReturn(BigDecimal expectedAnnualReturn) { this.expectedAnnualReturn = expectedAnnualReturn; }
    public BigDecimal getTargetRetirementAmount() { return targetRetirementAmount; }
    public void setTargetRetirementAmount(BigDecimal targetRetirementAmount) { this.targetRetirementAmount = targetRetirementAmount; }
    public Integer getCurrentAge() { return currentAge; }
    public void setCurrentAge(Integer currentAge) { this.currentAge = currentAge; }
    public Integer getTargetRetirementAge() { return targetRetirementAge; }
    public void setTargetRetirementAge(Integer targetRetirementAge) { this.targetRetirementAge = targetRetirementAge; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public List<RetirementContribution> getContributions() { return contributions; }
    public void setContributions(List<RetirementContribution> contributions) { this.contributions = contributions; }
}
