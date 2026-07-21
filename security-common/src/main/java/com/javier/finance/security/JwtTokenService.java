package com.javier.finance.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;

public class JwtTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String ISSUER_DEFAULT = "personal-finance-tracker";
    private static final String SECRET_DEFAULT = "dev-only-change-this-secret-key-for-class-demo";

    private final Environment environment;
    private final ObjectMapper objectMapper;

    public JwtTokenService(Environment environment, ObjectMapper objectMapper) {
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    public String createToken(Long userId, String username, String role) {
        long minutes = environment.getProperty("finance.security.token-minutes", Long.class, 120L);
        return createToken(userId, username, role, Duration.ofMinutes(minutes));
    }

    public String createToken(Long userId, String username, String role, Duration ttl) {
        Instant now = Instant.now();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", issuer());
        payload.put("sub", username);
        payload.put("userId", userId);
        payload.put("role", role);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plus(ttl).getEpochSecond());

        String encodedHeader = base64Url(toJsonBytes(header));
        String encodedPayload = base64Url(toJsonBytes(payload));
        String signingInput = encodedHeader + "." + encodedPayload;
        return signingInput + "." + sign(signingInput);
    }

    public FinanceUserPrincipal parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token format");
            }

            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = sign(signingInput);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new IllegalArgumentException("Invalid token signature");
            }

            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<Map<String, Object>>() {});

            if (!issuer().equals(String.valueOf(payload.get("iss")))) {
                throw new IllegalArgumentException("Invalid token issuer");
            }

            long exp = numberValue(payload.get("exp"));
            if (Instant.now().getEpochSecond() >= exp) {
                throw new IllegalArgumentException("Token has expired");
            }

            Long userId = numberValue(payload.get("userId"));
            String username = String.valueOf(payload.get("sub"));
            String role = String.valueOf(payload.getOrDefault("role", "USER"));
            return new FinanceUserPrincipal(userId, username, role);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid authorization token", ex);
        }
    }

    private byte[] toJsonBytes(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create token", ex);
        }
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return base64Url(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String secret() {
        return environment.getProperty("finance.security.jwt-secret", SECRET_DEFAULT);
    }

    private String issuer() {
        return environment.getProperty("finance.security.issuer", ISSUER_DEFAULT);
    }

    private long numberValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < leftBytes.length; i++) {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }
}
