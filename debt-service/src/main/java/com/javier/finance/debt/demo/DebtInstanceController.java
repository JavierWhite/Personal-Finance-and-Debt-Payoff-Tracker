package com.javier.finance.debt.demo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debts/demo")
public class DebtInstanceController {

    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${spring.application.name:debt-service}")
    private String applicationName;

    @Value("${server.port:8082}")
    private String port;

    @GetMapping("/instance")
    public Map<String, String> instance(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestHeader(value = "X-Gateway-Token-Fingerprint", required = false)
        String gatewayFingerprint
    ) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("service", applicationName);
        response.put("instance", System.getenv().getOrDefault("HOSTNAME", "local"));
        response.put("port", port);
        response.put("tokenFingerprint", fingerprint(authorization));
        response.put("gatewayFingerprint",
            gatewayFingerprint == null ? "missing" : gatewayFingerprint);
        return response;
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
