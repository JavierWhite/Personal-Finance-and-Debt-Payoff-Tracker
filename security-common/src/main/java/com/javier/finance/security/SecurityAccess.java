package com.javier.finance.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityAccess {
    private SecurityAccess() {
    }

    public static FinanceUserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof FinanceUserPrincipal principal)) {
            throw new AccessDeniedException("Authentication is required");
        }
        return principal;
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(currentUser().getRole());
    }

    public static void requireSelfOrAdmin(Long userId) {
        FinanceUserPrincipal principal = currentUser();
        if ("ADMIN".equalsIgnoreCase(principal.getRole())) {
            return;
        }
        if (principal.getUserId() != null && principal.getUserId().equals(userId)) {
            return;
        }
        throw new AccessDeniedException("You are not authorized to access this user's records");
    }
}
