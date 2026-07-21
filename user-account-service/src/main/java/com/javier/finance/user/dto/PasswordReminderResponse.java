package com.javier.finance.user.dto;

import java.time.LocalDateTime;

public record PasswordReminderResponse(
        String message,
        String resetToken,
        LocalDateTime expiresAt) {
}
