package com.javier.finance.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javier.finance.user.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
