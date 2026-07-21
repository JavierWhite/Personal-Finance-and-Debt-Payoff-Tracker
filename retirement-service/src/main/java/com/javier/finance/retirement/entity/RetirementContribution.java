package com.javier.finance.retirement.entity;

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
@Table(name = "retirement_contributions")
public class RetirementContribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false)
    private LocalDate contributionDate;
    @Column(length = 240)
    private String note;
    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "retirement_account_id", nullable = false)
    private RetirementAccount retirementAccount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getContributionDate() { return contributionDate; }
    public void setContributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public RetirementAccount getRetirementAccount() { return retirementAccount; }
    public void setRetirementAccount(RetirementAccount retirementAccount) { this.retirementAccount = retirementAccount; }
}
