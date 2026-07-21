package com.javier.finance.analytics.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javier.finance.analytics.dto.ChartDataPoint;
import com.javier.finance.analytics.dto.FinanceSummaryResponse;
import com.javier.finance.analytics.dto.MonthlyFinanceSnapshotRequest;
import com.javier.finance.analytics.entity.MonthlyFinanceSnapshot;
import com.javier.finance.analytics.exception.ResourceNotFoundException;
import com.javier.finance.analytics.repository.MonthlyFinanceSnapshotRepository;

@Service
@Transactional
public class AnalyticsService {
    private final MonthlyFinanceSnapshotRepository repository;

    public AnalyticsService(MonthlyFinanceSnapshotRepository repository) {
        this.repository = repository;
    }

    public MonthlyFinanceSnapshot create(MonthlyFinanceSnapshotRequest request) {
        if (repository.existsByUserIdAndMonth(request.userId(), request.month())) {
            throw new IllegalArgumentException("A snapshot already exists for this user and month");
        }
        MonthlyFinanceSnapshot snapshot = new MonthlyFinanceSnapshot();
        apply(snapshot, request);
        return repository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public MonthlyFinanceSnapshot findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Finance snapshot " + id + " was not found"));
    }

    @Transactional(readOnly = true)
    public List<MonthlyFinanceSnapshot> findByUserId(Long userId) {
        return repository.findByUserIdOrderByMonthAsc(userId);
    }

    public MonthlyFinanceSnapshot update(Long id, MonthlyFinanceSnapshotRequest request) {
        MonthlyFinanceSnapshot snapshot = findById(id);
        if (repository.existsByUserIdAndMonthAndIdNot(request.userId(), request.month(), id)) {
            throw new IllegalArgumentException("A snapshot already exists for this user and month");
        }
        apply(snapshot, request);
        return repository.save(snapshot);
    }

    public void delete(Long id) { repository.delete(findById(id)); }

    @Transactional(readOnly = true)
    public FinanceSummaryResponse summary(Long userId) {
        MonthlyFinanceSnapshot latest = repository.findFirstByUserIdOrderByMonthDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No finance snapshots exist for user " + userId));
        return new FinanceSummaryResponse(latest.getUserId(), latest.getMonth(), latest.getTotalDebt(),
                latest.getTotalSavings(), latest.getRetirementBalance(), latest.getMonthlyIncome(),
                latest.getNetWorthEstimate());
    }

    @Transactional(readOnly = true)
    public List<ChartDataPoint> debtChart(Long userId) {
        return chart(userId, MonthlyFinanceSnapshot::getTotalDebt);
    }

    @Transactional(readOnly = true)
    public List<ChartDataPoint> savingsChart(Long userId) {
        return chart(userId, MonthlyFinanceSnapshot::getTotalSavings);
    }

    @Transactional(readOnly = true)
    public List<ChartDataPoint> netWorthChart(Long userId) {
        return chart(userId, MonthlyFinanceSnapshot::getNetWorthEstimate);
    }

    private List<ChartDataPoint> chart(Long userId, Function<MonthlyFinanceSnapshot, BigDecimal> value) {
        return repository.findByUserIdOrderByMonthAsc(userId).stream()
                .map(snapshot -> new ChartDataPoint(snapshot.getMonth(), value.apply(snapshot)))
                .toList();
    }

    private void apply(MonthlyFinanceSnapshot snapshot, MonthlyFinanceSnapshotRequest request) {
        snapshot.setUserId(request.userId());
        snapshot.setMonth(request.month());
        snapshot.setTotalDebt(request.totalDebt());
        snapshot.setTotalSavings(request.totalSavings());
        snapshot.setRetirementBalance(request.retirementBalance());
        snapshot.setMonthlyIncome(request.monthlyIncome());
        snapshot.setNetWorthEstimate(request.totalSavings()
                .add(request.retirementBalance())
                .subtract(request.totalDebt()));
    }
}
