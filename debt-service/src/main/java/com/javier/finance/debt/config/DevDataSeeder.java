package com.javier.finance.debt.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.javier.finance.debt.dto.DebtAccountRequest;
import com.javier.finance.debt.entity.DebtStatus;
import com.javier.finance.debt.entity.DebtType;
import com.javier.finance.debt.repository.DebtAccountRepository;
import com.javier.finance.debt.service.DebtAccountService;

@Configuration
@Profile("dev")
public class DevDataSeeder {
    @Bean
    CommandLineRunner seedDebts(DebtAccountRepository repository, DebtAccountService service) {
        return args -> {
            if (repository.count() == 0) {
                service.create(new DebtAccountRequest(1L, "Demo Credit Card", DebtType.CREDIT_CARD,
                        new BigDecimal("3000.00"), new BigDecimal("3000.00"), new BigDecimal("22.000"),
                        new BigDecimal("90.00"), LocalDate.now().plusDays(14), DebtStatus.ACTIVE));
            }
        };
    }
}
