package com.javier.finance.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.javier.finance.security.InputSecurityValidator;
import com.javier.finance.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javier.finance.user.dto.LoginRequest;
import com.javier.finance.user.dto.LoginResponse;
import com.javier.finance.user.dto.PasswordReminderResponse;
import com.javier.finance.user.dto.UserAccountRequest;
import com.javier.finance.user.entity.UserAccount;
import com.javier.finance.user.entity.UserProfile;
import com.javier.finance.user.entity.UserRole;
import com.javier.finance.user.exception.ResourceNotFoundException;
import com.javier.finance.user.repository.IncomeRecordRepository;
import com.javier.finance.user.repository.UserAccountRepository;

@Service
@Transactional
public class UserAccountService {
    private final UserAccountRepository repository;
    private final IncomeRecordRepository incomeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public UserAccountService(
            UserAccountRepository repository,
            IncomeRecordRepository incomeRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.repository = repository;
        this.incomeRepository = incomeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public UserAccount create(UserAccountRequest request) {
        validateUnique(request, null);
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required when creating an account");
        }
        UserAccount account = new UserAccount();
        apply(account, request);
        account.setRole(UserRole.USER);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        return repository.save(account);
    }

    public LoginResponse register(UserAccountRequest request) {
        return toLoginResponse(create(request));
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public UserAccount findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User account " + id + " was not found"));
    }

    public UserAccount update(Long id, UserAccountRequest request) {
        UserAccount account = findById(id);
        validateUnique(request, id);
        apply(account, request);
        if (request.password() != null && !request.password().isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return repository.save(account);
    }

    public LoginResponse authenticate(LoginRequest request) {
        String username = InputSecurityValidator.safeUsername(request.username());
        UserAccount account = repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!account.isActive()) {
            throw new IllegalArgumentException("This account is inactive");
        }
        if (account.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return toLoginResponse(account);
    }

    public PasswordReminderResponse createPasswordReminder(String email) {
        String safeEmail = InputSecurityValidator.safeText("email", email, 120).toLowerCase();
        UserAccount account = repository.findByEmailIgnoreCase(safeEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account exists for that email address"));
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        account.setPasswordResetToken(token);
        account.setPasswordResetExpiresAt(expiresAt);
        repository.save(account);
        return new PasswordReminderResponse(
                "Password reset token created. In production this token would be sent by email.",
                token,
                expiresAt);
    }

    public LoginResponse resetPassword(String resetToken, String newPassword) {
        String safeResetToken = InputSecurityValidator.safeResetToken(resetToken);
        UserAccount account = repository.findByPasswordResetToken(safeResetToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));
        if (account.getPasswordResetExpiresAt() == null
                || account.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setPasswordResetToken(null);
        account.setPasswordResetExpiresAt(null);
        return toLoginResponse(repository.save(account));
    }

    public void delete(Long id) {
        UserAccount account = findById(id);
        incomeRepository.deleteByUserId(id);
        repository.delete(account);
    }

    private LoginResponse toLoginResponse(UserAccount account) {
        UserProfile profile = account.getProfile();
        String role = account.getRole() == null ? UserRole.USER.name() : account.getRole().name();
        String token = jwtTokenService.createToken(account.getId(), account.getUsername(), role);
        return new LoginResponse(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                profile == null ? "" : profile.getFirstName(),
                profile == null ? "" : profile.getLastName(),
                account.isActive(),
                role,
                token);
    }

    private void validateUnique(UserAccountRequest request, Long id) {
        String username = InputSecurityValidator.safeUsername(request.username());
        String email = InputSecurityValidator.safeText("email", request.email(), 120).toLowerCase();
        boolean usernameExists = id == null
                ? repository.existsByUsernameIgnoreCase(username)
                : repository.existsByUsernameIgnoreCaseAndIdNot(username, id);
        boolean emailExists = id == null
                ? repository.existsByEmailIgnoreCase(email)
                : repository.existsByEmailIgnoreCaseAndIdNot(email, id);
        if (usernameExists) {
            throw new IllegalArgumentException("Username is already in use");
        }
        if (emailExists) {
            throw new IllegalArgumentException("Email is already in use");
        }
    }

    private void apply(UserAccount account, UserAccountRequest request) {
        account.setUsername(InputSecurityValidator.safeUsername(request.username()));
        account.setEmail(InputSecurityValidator.safeText("email", request.email(), 120).toLowerCase());
        account.setActive(request.active() == null || request.active());

        UserProfile profile = account.getProfile() == null ? new UserProfile() : account.getProfile();
        profile.setFirstName(InputSecurityValidator.safeText("first name", request.firstName(), 50));
        profile.setLastName(InputSecurityValidator.safeText("last name", request.lastName(), 50));
        account.attachProfile(profile);
    }
}
