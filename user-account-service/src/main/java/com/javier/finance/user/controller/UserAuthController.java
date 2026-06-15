package com.javier.finance.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javier.finance.user.dto.LoginRequest;
import com.javier.finance.user.dto.LoginResponse;
import com.javier.finance.user.service.UserAccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class UserAuthController {
    private final UserAccountService service;

    public UserAuthController(UserAccountService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.authenticate(request);
    }
}
