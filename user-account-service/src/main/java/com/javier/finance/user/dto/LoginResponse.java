package com.javier.finance.user.dto;

public record LoginResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean active,
        String role,
        String token) {
}
