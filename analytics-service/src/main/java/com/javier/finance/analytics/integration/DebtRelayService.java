package com.javier.finance.analytics.integration;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DebtRelayService {

    private final RestClient.Builder restClientBuilder;

    public DebtRelayService(
        @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder
    ) {
        this.restClientBuilder = restClientBuilder;
    }

    @CircuitBreaker(name = "debtService", fallbackMethod = "debtFallback")
    public RelayResult getDebtsForUser(Long userId, String authorization) {
        String body = restClientBuilder.build()
            .get()
            .uri("http://debt-service/debts/user/{userId}", userId)
            .header(HttpHeaders.AUTHORIZATION, authorization)
            .retrieve()
            .body(String.class);

        return new RelayResult("UP", body == null ? "[]" : body);
    }

    private RelayResult debtFallback(Long userId, String authorization, Throwable exception) {
        String message = "{\"status\":\"DEGRADED\",\"service\":\"debt-service\","
            + "\"userId\":" + userId + ","
            + "\"message\":\"Debt data is temporarily unavailable\"}";

        return new RelayResult("DEGRADED", message);
    }

    public record RelayResult(String status, String body) {
    }
}
