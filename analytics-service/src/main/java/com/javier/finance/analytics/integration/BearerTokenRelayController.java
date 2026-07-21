package com.javier.finance.analytics.integration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics/demo")
public class BearerTokenRelayController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final DebtRelayService debtRelayService;

    public BearerTokenRelayController(DebtRelayService debtRelayService) {
        this.debtRelayService = debtRelayService;
    }

    @GetMapping(value = "/debts/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> relayDebtRequest(
        @PathVariable Long userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestHeader(value = "X-Gateway-Token-Fingerprint", required = false)
        String gatewayFingerprint
    ) {
        DebtRelayService.RelayResult result =
            debtRelayService.getDebtsForUser(userId, authorization);

        String localFingerprint = fingerprint(authorization);

        return ResponseEntity.ok()
            .header("X-Token-Relay", "forwarded")
            .header("X-Token-Fingerprint", localFingerprint)
            .header("X-Gateway-Token-Fingerprint-Seen",
                gatewayFingerprint == null ? "missing" : gatewayFingerprint)
            .header("X-Resilience-Status", result.status())
            .body(result.body());
    }

    private String fingerprint(String authorization) {
        String token = authorization.startsWith(BEARER_PREFIX)
            ? authorization.substring(BEARER_PREFIX.length())
            : authorization;

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 6);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
