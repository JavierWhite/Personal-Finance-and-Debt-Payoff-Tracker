package com.javier.finance.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class FinanceSecuritySupportConfiguration {
    @Bean
    public JwtTokenService jwtTokenService(Environment environment, ObjectMapper objectMapper) {
        return new JwtTokenService(environment, objectMapper);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        return new JwtAuthenticationFilter(jwtTokenService);
    }
}
