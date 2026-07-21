package com.javier.finance.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class BearerTokenAuditFilter implements GlobalFilter, Ordered {

    public static final String TOKEN_FINGERPRINT_HEADER = "X-Gateway-Token-Fingerprint";

    private static final Logger log = LoggerFactory.getLogger(BearerTokenAuditFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            log.debug("Gateway request has no bearer token, path={}", exchange.getRequest().getPath());
            return chain.filter(exchange);
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        String fingerprint = fingerprint(token);

        ServerHttpRequest request = exchange.getRequest().mutate()
            .headers(headers -> {
                headers.remove(TOKEN_FINGERPRINT_HEADER);
                headers.add(TOKEN_FINGERPRINT_HEADER, fingerprint);
            })
            .build();

        log.info("Relaying bearer token fingerprint={} path={}",
            fingerprint,
            exchange.getRequest().getPath());

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private String fingerprint(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 6);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
