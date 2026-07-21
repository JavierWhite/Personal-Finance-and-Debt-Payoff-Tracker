package com.javier.finance.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @NotBlank @Size(min = 20, max = 128) @Pattern(regexp = "^[A-Za-z0-9]+$") String resetToken,
        @NotBlank @Size(min = 8) String newPassword) {
}
