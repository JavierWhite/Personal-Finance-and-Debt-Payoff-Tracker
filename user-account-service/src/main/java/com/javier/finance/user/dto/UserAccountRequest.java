package com.javier.finance.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserAccountRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        Boolean active) {
}
