package com.javier.finance.analytics.config;

import java.math.BigDecimal;
import java.time.YearMonth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.javier.finance.analytics.dto.MonthlyFinanceSnapshotRequest;
import com.javier.finance.analytics.repository.MonthlyFinanceSnapshotRepository;
import com.javier.finance.analytics.service.AnalyticsService;

@Configuration
@Profile("dev")
public class DevDataSeeder {
    @Bean
    CommandLineRunner seedSnapshots(MonthlyFinanceSnapshotRepository repository, AnalyticsService service) {
        return args -> {
            if (repository.count() == 0) {
                service.create(new MonthlyFinanceSnapshotRequest(1L, YearMonth.now().minusMonths(1),
                        new BigDecimal("3000.00"), BigDecimal.ZERO, new BigDecimal("2000.00"),
                        new BigDecimal("5000.00")));
            }
        };
    }
}
