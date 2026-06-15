package com.javier.finance.retirement.controller;

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

import com.javier.finance.retirement.dto.RetirementAccountRequest;
import com.javier.finance.retirement.dto.RetirementContributionRequest;
import com.javier.finance.retirement.dto.RetirementProjectionResponse;
import com.javier.finance.retirement.entity.RetirementAccount;
import com.javier.finance.retirement.service.RetirementAccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/retirement-accounts")
public class RetirementAccountController {
    private final RetirementAccountService service;

    public RetirementAccountController(RetirementAccountService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<RetirementAccount> create(@Valid @RequestBody RetirementAccountRequest request) {
        RetirementAccount created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<RetirementAccount> findAll() { return service.findAll(); }

    @GetMapping("/user/{userId}")
    public List<RetirementAccount> findByUser(@PathVariable Long userId) { return service.findByUserId(userId); }

    @GetMapping("/{id}")
    public RetirementAccount findById(@PathVariable Long id) { return service.findById(id); }

    @PutMapping("/{id}")
    public RetirementAccount update(@PathVariable Long id, @Valid @RequestBody RetirementAccountRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/contributions")
    public RetirementAccount addContribution(@PathVariable Long id,
            @Valid @RequestBody RetirementContributionRequest request) {
        return service.addContribution(id, request);
    }

    @GetMapping("/{id}/projection")
    public RetirementProjectionResponse projection(@PathVariable Long id) {
        return service.project(id);
    }
}
