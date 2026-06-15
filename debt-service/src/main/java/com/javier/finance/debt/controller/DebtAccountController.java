package com.javier.finance.debt.controller;

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

import com.javier.finance.debt.dto.DebtAccountRequest;
import com.javier.finance.debt.dto.DebtPaymentRequest;
import com.javier.finance.debt.entity.DebtAccount;
import com.javier.finance.debt.service.DebtAccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/debts")
public class DebtAccountController {
    private final DebtAccountService service;

    public DebtAccountController(DebtAccountService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<DebtAccount> create(@Valid @RequestBody DebtAccountRequest request) {
        DebtAccount created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<DebtAccount> findAll() { return service.findAll(); }

    @GetMapping("/user/{userId}")
    public List<DebtAccount> findByUser(@PathVariable Long userId) { return service.findByUserId(userId); }

    @GetMapping("/{id}")
    public DebtAccount findById(@PathVariable Long id) { return service.findById(id); }

    @PutMapping("/{id}")
    public DebtAccount update(@PathVariable Long id, @Valid @RequestBody DebtAccountRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payments")
    public DebtAccount addPayment(@PathVariable Long id, @Valid @RequestBody DebtPaymentRequest request) {
        return service.addPayment(id, request);
    }
}
