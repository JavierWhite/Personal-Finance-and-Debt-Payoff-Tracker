package com.javier.finance.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordReminderRequest(
        @Email @NotBlank @Size(max = 120) String email) {
}
