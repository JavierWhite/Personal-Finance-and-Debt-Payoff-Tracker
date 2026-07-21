package com.javier.finance.savings.controller;

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

import com.javier.finance.savings.dto.SavingsContributionRequest;
import com.javier.finance.savings.dto.SavingsGoalRequest;
import com.javier.finance.savings.entity.SavingsGoal;
import com.javier.finance.savings.service.SavingsGoalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/savings-goals")
public class SavingsGoalController {
    private final SavingsGoalService service;

    public SavingsGoalController(SavingsGoalService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<SavingsGoal> create(@Valid @RequestBody SavingsGoalRequest request) {
        SecurityAccess.requireSelfOrAdmin(request.userId());
        SavingsGoal created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SavingsGoal> findAll() { return service.findAll(); }

    @GetMapping("/user/{userId}")
    public List<SavingsGoal> findByUser(@PathVariable Long userId) {
        SecurityAccess.requireSelfOrAdmin(userId);
        return service.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public SavingsGoal findById(@PathVariable Long id) {
        SavingsGoal goal = service.findById(id);
        SecurityAccess.requireSelfOrAdmin(goal.getUserId());
        return goal;
    }

    @PutMapping("/{id}")
    public SavingsGoal update(@PathVariable Long id, @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoal goal = service.findById(id);
        SecurityAccess.requireSelfOrAdmin(goal.getUserId());
        SecurityAccess.requireSelfOrAdmin(request.userId());
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        SavingsGoal goal = service.findById(id);
        SecurityAccess.requireSelfOrAdmin(goal.getUserId());
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/contributions")
    public SavingsGoal addContribution(@PathVariable Long id,
            @Valid @RequestBody SavingsContributionRequest request) {
        SavingsGoal goal = service.findById(id);
        SecurityAccess.requireSelfOrAdmin(goal.getUserId());
        return service.addContribution(id, request);
    }
}
