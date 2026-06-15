package com.javier.finance.user.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javier.finance.user.dto.LoginRequest;
import com.javier.finance.user.dto.LoginResponse;
import com.javier.finance.user.dto.UserAccountRequest;
import com.javier.finance.user.entity.UserAccount;
import com.javier.finance.user.entity.UserProfile;
import com.javier.finance.user.exception.ResourceNotFoundException;
import com.javier.finance.user.repository.IncomeRecordRepository;
import com.javier.finance.user.repository.UserAccountRepository;

@Service
@Transactional
public class UserAccountService {
    private final UserAccountRepository repository;
    private final IncomeRecordRepository incomeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(
            UserAccountRepository repository,
            IncomeRecordRepository incomeRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.incomeRepository = incomeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount create(UserAccountRequest request) {
        validateUnique(request, null);
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required when creating an account");
        }
        UserAccount account = new UserAccount();
        apply(account, request);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        return repository.save(account);
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
        UserAccount account = repository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!account.isActive()) {
            throw new IllegalArgumentException("This account is inactive");
        }
        if (account.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        UserProfile profile = account.getProfile();
        return new LoginResponse(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                profile == null ? "" : profile.getFirstName(),
                profile == null ? "" : profile.getLastName(),
                account.isActive());
    }

    public void delete(Long id) {
        UserAccount account = findById(id);
        incomeRepository.deleteByUserId(id);
        repository.delete(account);
    }

    private void validateUnique(UserAccountRequest request, Long id) {
        boolean usernameExists = id == null
                ? repository.existsByUsernameIgnoreCase(request.username())
                : repository.existsByUsernameIgnoreCaseAndIdNot(request.username(), id);
        boolean emailExists = id == null
                ? repository.existsByEmailIgnoreCase(request.email())
                : repository.existsByEmailIgnoreCaseAndIdNot(request.email(), id);
        if (usernameExists) {
            throw new IllegalArgumentException("Username is already in use");
        }
        if (emailExists) {
            throw new IllegalArgumentException("Email is already in use");
        }
    }

    private void apply(UserAccount account, UserAccountRequest request) {
        account.setUsername(request.username());
        account.setEmail(request.email());
        account.setActive(request.active() == null || request.active());

        UserProfile profile = account.getProfile() == null ? new UserProfile() : account.getProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        account.attachProfile(profile);
    }
}
