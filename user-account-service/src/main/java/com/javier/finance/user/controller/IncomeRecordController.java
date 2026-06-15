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
        IncomeRecord created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<IncomeRecord> findAll() {
        return service.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<IncomeRecord> findByUser(@PathVariable Long userId) {
        return service.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public IncomeRecord findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public IncomeRecord update(@PathVariable Long id, @Valid @RequestBody IncomeRecordRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
