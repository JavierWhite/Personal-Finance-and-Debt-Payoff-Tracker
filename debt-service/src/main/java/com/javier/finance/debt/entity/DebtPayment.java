package com.javier.finance.debt.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "debt_payments")
public class DebtPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(length = 240)
    private String note;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "debt_account_id", nullable = false)
    private DebtAccount debtAccount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public DebtAccount getDebtAccount() { return debtAccount; }
    public void setDebtAccount(DebtAccount debtAccount) { this.debtAccount = debtAccount; }
}
