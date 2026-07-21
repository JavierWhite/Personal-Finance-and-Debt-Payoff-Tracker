package com.javier.finance.retirement.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.javier.finance.retirement.dto.RetirementAccountRequest;
import com.javier.finance.retirement.entity.RetirementAccountType;
import com.javier.finance.retirement.repository.RetirementAccountRepository;
import com.javier.finance.retirement.service.RetirementAccountService;

@Configuration
@Profile("dev")
public class DevDataSeeder {
    @Bean
    CommandLineRunner seedRetirement(RetirementAccountRepository repository, RetirementAccountService service) {
        return args -> {
            if (repository.count() == 0) {
                service.create(new RetirementAccountRequest(1L, "Roth IRA", RetirementAccountType.ROTH_IRA,
                        new BigDecimal("2000.00"), new BigDecimal("150.00"), new BigDecimal("7.000"),
                        new BigDecimal("750000.00"), 35, 65));
            }
        };
    }
}
