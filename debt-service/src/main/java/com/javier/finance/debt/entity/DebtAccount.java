package com.javier.finance.debt.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "debt_accounts")
public class DebtAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 120)
    private String debtName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DebtType debtType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal originalBalance;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal currentBalance;

    @Column(nullable = false, precision = 7, scale = 3)
    private BigDecimal interestRate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal minimumPayment;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DebtStatus status = DebtStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "debtAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DebtPayment> payments = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (createdDate == null) createdDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDebtName() { return debtName; }
    public void setDebtName(String debtName) { this.debtName = debtName; }
    public DebtType getDebtType() { return debtType; }
    public void setDebtType(DebtType debtType) { this.debtType = debtType; }
    public BigDecimal getOriginalBalance() { return originalBalance; }
    public void setOriginalBalance(BigDecimal originalBalance) { this.originalBalance = originalBalance; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public BigDecimal getMinimumPayment() { return minimumPayment; }
    public void setMinimumPayment(BigDecimal minimumPayment) { this.minimumPayment = minimumPayment; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public DebtStatus getStatus() { return status; }
    public void setStatus(DebtStatus status) { this.status = status; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public List<DebtPayment> getPayments() { return payments; }
    public void setPayments(List<DebtPayment> payments) { this.payments = payments; }
}
