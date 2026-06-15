package com.javier.finance.user.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.javier.finance.user.dto.UserAccountRequest;
import com.javier.finance.user.repository.UserAccountRepository;
import com.javier.finance.user.service.UserAccountService;

@Configuration
@Profile("dev")
public class DevDataSeeder {
    @Bean
    CommandLineRunner seedUsers(UserAccountRepository repository, UserAccountService service) {
        return args -> {
            if (repository.count() == 0) {
                service.create(new UserAccountRequest(
                        "javier", "javier@example.com", "Javier", "White", true));
            }
        };
    }
}
