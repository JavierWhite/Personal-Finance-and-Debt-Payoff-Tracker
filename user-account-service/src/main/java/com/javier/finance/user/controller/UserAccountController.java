package com.javier.finance.user.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.javier.finance.user.dto.UserAccountRequest;
import com.javier.finance.user.entity.UserAccount;
import com.javier.finance.user.service.UserAccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserAccountController {
    private final UserAccountService service;

    public UserAccountController(UserAccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserAccount> create(@Valid @RequestBody UserAccountRequest request) {
        UserAccount created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<UserAccount> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public UserAccount findById(@PathVariable Long id) { return service.findById(id); }

    @PutMapping("/{id}")
    public UserAccount update(@PathVariable Long id, @Valid @RequestBody UserAccountRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
