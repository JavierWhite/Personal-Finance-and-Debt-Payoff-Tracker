package com.javier.finance.savings.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.javier.finance.savings.dto.SavingsGoalRequest;
import com.javier.finance.savings.entity.SavingsStatus;
import com.javier.finance.savings.repository.SavingsGoalRepository;
import com.javier.finance.savings.service.SavingsGoalService;

@Configuration
@Profile("dev")
public class DevDataSeeder {
    @Bean
    CommandLineRunner seedSavings(SavingsGoalRepository repository, SavingsGoalService service) {
        return args -> {
            if (repository.count() == 0) {
                service.create(new SavingsGoalRequest(1L, "Emergency Fund", new BigDecimal("5000.00"),
                        BigDecimal.ZERO, LocalDate.now().plusYears(1), SavingsStatus.ACTIVE));
            }
        };
    }
}
