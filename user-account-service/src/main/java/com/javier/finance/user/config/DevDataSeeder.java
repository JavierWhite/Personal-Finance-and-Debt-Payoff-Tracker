package com.javier.finance.user.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.javier.finance.user.entity.IncomeFrequency;
import com.javier.finance.user.entity.IncomeRecord;
import com.javier.finance.user.entity.UserAccount;
import com.javier.finance.user.entity.UserProfile;
import com.javier.finance.user.entity.UserRole;
import com.javier.finance.user.repository.IncomeRecordRepository;
import com.javier.finance.user.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevDataSeeder {
    @Bean
    CommandLineRunner seedUsers(
            UserAccountRepository users,
            IncomeRecordRepository incomes,
            PasswordEncoder passwordEncoder) {
        return args -> {
            UserAccount user = createAccount(users, passwordEncoder, "javier", "javier@example.com", "Javier", "White", UserRole.USER);
            createAccount(users, passwordEncoder, "admin", "admin@example.com", "Admin", "User", UserRole.ADMIN);

            if (user != null && incomes.findByUserId(user.getId()).isEmpty()) {
                IncomeRecord income = new IncomeRecord();
                income.setUserId(user.getId());
                income.setSource("Primary Job");
                income.setGrossAmount(new BigDecimal("72000.00"));
                income.setFrequency(IncomeFrequency.YEARLY);
                income.setEffectiveDate(LocalDate.now());
                income.setActive(true);
                incomes.save(income);
            }
        };
    }

    private UserAccount createAccount(
            UserAccountRepository users,
            PasswordEncoder passwordEncoder,
            String username,
            String email,
            String firstName,
            String lastName,
            UserRole role) {
        if (users.existsByUsernameIgnoreCase(username)) {
            return users.findByUsernameIgnoreCase(username).orElse(null);
        }

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode("Password123!"));
        account.setRole(role);
        account.setActive(true);

        UserProfile profile = new UserProfile();
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        account.attachProfile(profile);

        return users.save(account);
    }
}
