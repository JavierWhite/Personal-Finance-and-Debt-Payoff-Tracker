package com.javier.finance.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javier.finance.user.dto.UserAccountRequest;
import com.javier.finance.user.entity.UserAccount;
import com.javier.finance.user.entity.UserProfile;
import com.javier.finance.user.exception.ResourceNotFoundException;
import com.javier.finance.user.repository.UserAccountRepository;

@Service
@Transactional
public class UserAccountService {
    private final UserAccountRepository repository;

    public UserAccountService(UserAccountRepository repository) {
        this.repository = repository;
    }

    public UserAccount create(UserAccountRequest request) {
        validateUnique(request, null);
        UserAccount account = new UserAccount();
        apply(account, request);
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
        return repository.save(account);
    }

    public void delete(Long id) {
        UserAccount account = findById(id);
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
