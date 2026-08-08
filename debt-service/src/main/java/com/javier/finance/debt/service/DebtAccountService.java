package com.javier.finance.debt.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.javier.finance.security.InputSecurityValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javier.finance.debt.dto.DebtAccountRequest;
import com.javier.finance.debt.dto.DebtPaymentRequest;
import com.javier.finance.debt.entity.DebtAccount;
import com.javier.finance.debt.entity.DebtPayment;
import com.javier.finance.debt.entity.DebtStatus;
import com.javier.finance.debt.exception.ResourceNotFoundException;
import com.javier.finance.debt.messaging.DebtPaymentEventPublisher;
import com.javier.finance.debt.messaging.DebtPaymentRecordedEvent;
import com.javier.finance.debt.repository.DebtAccountRepository;
import com.javier.finance.debt.repository.DebtPaymentRepository;

@Service
@Transactional
public class DebtAccountService {
    private final DebtAccountRepository debtRepository;
    private final DebtPaymentRepository paymentRepository;
    private final DebtPaymentEventPublisher paymentEventPublisher;

    public DebtAccountService(
            DebtAccountRepository debtRepository,
            DebtPaymentRepository paymentRepository,
            DebtPaymentEventPublisher paymentEventPublisher) {
        this.debtRepository = debtRepository;
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    public DebtAccount create(DebtAccountRequest request) {
        DebtAccount debt = new DebtAccount();
        apply(debt, request, true);
        return debtRepository.save(debt);
    }

    @Transactional(readOnly = true)
    public List<DebtAccount> findAll() { return debtRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<DebtAccount> findByUserId(Long userId) {
        return debtRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public DebtAccount findById(Long id) {
        return debtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Debt account " + id + " was not found"));
    }

    public DebtAccount update(Long id, DebtAccountRequest request) {
        DebtAccount debt = findById(id);
        apply(debt, request, false);
        return debtRepository.save(debt);
    }

    public void delete(Long id) {
        debtRepository.delete(findById(id));
    }

    public DebtAccount addPayment(Long debtId, DebtPaymentRequest request) {
        DebtAccount debt = findById(debtId);
        if (debt.getStatus() == DebtStatus.CLOSED) {
            throw new IllegalArgumentException("Payments cannot be added to a closed debt");
        }
        if (request.amount().compareTo(debt.getCurrentBalance()) > 0) {
            throw new IllegalArgumentException("Payment cannot exceed the current balance");
        }

        DebtPayment payment = new DebtPayment();
        payment.setAmount(request.amount());
        payment.setPaymentDate(request.paymentDate() == null ? LocalDate.now() : request.paymentDate());
        payment.setNote(InputSecurityValidator.safeText("payment note", request.note(), 250));
        payment.setDebtAccount(debt);
        paymentRepository.save(payment);
        debt.getPayments().add(payment);

        BigDecimal newBalance = debt.getCurrentBalance().subtract(request.amount());
        debt.setCurrentBalance(newBalance);
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus(DebtStatus.PAID_OFF);
        }

        DebtAccount savedDebt = debtRepository.save(debt);
        paymentEventPublisher.publish(new DebtPaymentRecordedEvent(
                UUID.randomUUID(),
                savedDebt.getUserId(),
                savedDebt.getId(),
                payment.getId(),
                payment.getAmount(),
                savedDebt.getCurrentBalance(),
                payment.getPaymentDate(),
                Instant.now()));
        return savedDebt;
    }

    private void apply(DebtAccount debt, DebtAccountRequest request, boolean creating) {
        debt.setUserId(request.userId());
        debt.setDebtName(InputSecurityValidator.safeText("debt name", request.debtName(), 100));
        debt.setDebtType(request.debtType());
        debt.setOriginalBalance(request.originalBalance());
        if (creating) {
            debt.setCurrentBalance(request.currentBalance() == null ? request.originalBalance() : request.currentBalance());
        } else if (request.currentBalance() != null) {
            debt.setCurrentBalance(request.currentBalance());
        }
        debt.setInterestRate(request.interestRate());
        debt.setMinimumPayment(request.minimumPayment());
        debt.setDueDate(request.dueDate());
        if (request.status() != null) debt.setStatus(request.status());
    }
}
