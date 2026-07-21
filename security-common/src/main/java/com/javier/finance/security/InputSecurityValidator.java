package com.javier.finance.security;

import java.util.regex.Pattern;

public final class InputSecurityValidator {
    private static final Pattern SQL_COMMENT_OR_TERMINATOR = Pattern.compile("(;|--|/\\*|\\*/)");
    private static final Pattern SQL_INJECTION_PHRASE = Pattern.compile(
            "(?i)(\\bunion\\s+select\\b"
                    + "|\\bselect\\s+.+\\s+from\\b"
                    + "|\\binsert\\s+into\\b"
                    + "|\\bdelete\\s+from\\b"
                    + "|\\bdrop\\s+table\\b"
                    + "|\\balter\\s+table\\b"
                    + "|\\btruncate\\s+table\\b"
                    + "|\\bupdate\\s+.+\\s+set\\b"
                    + "|\\bor\\s+1\\s*=\\s*1\\b"
                    + "|\\band\\s+1\\s*=\\s*1\\b)");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,40}$");
    private static final Pattern RESET_TOKEN = Pattern.compile("^[A-Za-z0-9]{20,128}$");

    private InputSecurityValidator() {
    }

    public static String safeUsername(String value) {
        String username = safeText("username", value, 40);
        if (!USERNAME.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Username may only contain letters, numbers, dots, underscores, and hyphens");
        }
        return username;
    }

    public static String safeResetToken(String value) {
        String token = safeText("reset token", value, 128);
        if (!RESET_TOKEN.matcher(token).matches()) {
            throw new IllegalArgumentException("Invalid password reset token format");
        }
        return token;
    }

    public static String safeText(String fieldName, String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be " + maxLength + " characters or fewer");
        }

        if (SQL_COMMENT_OR_TERMINATOR.matcher(trimmed).find()
                || SQL_INJECTION_PHRASE.matcher(trimmed).find()) {
            throw new IllegalArgumentException("Unsafe input detected in " + fieldName);
        }

        return trimmed;
    }
}
