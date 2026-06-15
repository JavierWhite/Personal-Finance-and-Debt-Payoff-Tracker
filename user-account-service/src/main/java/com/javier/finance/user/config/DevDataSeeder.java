package com.javier.finance.user.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.javier.finance.user.dto.IncomeRecordRequest;
import com.javier.finance.user.dto.UserAccountRequest;
import com.javier.finance.user.entity.IncomeFrequency;
import com.javier.finance.user.repository.UserAccountRepository;
import com.javier.finance.user.service.IncomeRecordService;
import com.javier.finance.user.service.UserAccountService;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@Profile("dev")
public class DevDataSeeder {
    @Bean
    CommandLineRunner seedUsers(
            UserAccountRepository repository,
            UserAccountService userService,
            IncomeRecordService incomeService) {
        return args -> {
            if (repository.count() == 0) {
                var user = userService.create(new UserAccountRequest(
                        "javier",
                        "javier@example.com",
                        "Javier",
                        "White",
                        "Password123!",
                        true));

                incomeService.create(new IncomeRecordRequest(
                        user.getId(),
                        "Primary Job",
                        new BigDecimal("72000.00"),
                        IncomeFrequency.YEARLY,
                        LocalDate.now(),
                        true));
            }
        };
    }
}
