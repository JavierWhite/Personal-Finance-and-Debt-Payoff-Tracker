package com.javier.finance.user.controller;

import java.net.URI;
import java.util.List;

import com.javier.finance.security.SecurityAccess;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.javier.finance.user.dto.IncomeRecordRequest;
import com.javier.finance.user.entity.IncomeRecord;
import com.javier.finance.user.service.IncomeRecordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/incomes")
public class IncomeRecordController {
    private final IncomeRecordService service;

    public IncomeRecordController(IncomeRecordService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<IncomeRecord> create(@Valid @RequestBody IncomeRecordRequest request) {
        SecurityAccess.requireSelfOrAdmin(request.userId());
        IncomeRecord created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<IncomeRecord> findAll() {
        return service.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<IncomeRecord> findByUser(@PathVariable Long userId) {
        SecurityAccess.requireSelfOrAdmin(userId);
        return service.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public IncomeRecord findById(@PathVariable Long id) {
        IncomeRecord record = service.findById(id);
        SecurityAccess.requireSelfOrAdmin(record.getUserId());
        return record;
    }

    @PutMapping("/{id}")
    public IncomeRecord update(@PathVariable Long id, @Valid @RequestBody IncomeRecordRequest request) {
        IncomeRecord record = service.findById(id);
        SecurityAccess.requireSelfOrAdmin(record.getUserId());
        SecurityAccess.requireSelfOrAdmin(request.userId());
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        IncomeRecord record = service.findById(id);
        SecurityAccess.requireSelfOrAdmin(record.getUserId());
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
