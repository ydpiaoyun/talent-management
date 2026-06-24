package com.talent.controller;

import com.talent.common.R;
import com.talent.entity.ScoringPlan;
import com.talent.service.ScoringService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scoring")
public class ScoringController {

    private final ScoringService service;

    public ScoringController(ScoringService service) {
        this.service = service;
    }

    @GetMapping("/plan/list")
    public R<List<ScoringPlan>> listPlans() {
        return R.ok(service.listPlans());
    }

    @GetMapping("/plan/{id}")
    public R<ScoringPlan> getPlan(@PathVariable Long id) {
        return R.ok(service.getPlan(id));
    }

    @PostMapping("/plan")
    public R<?> savePlan(@RequestBody ScoringPlan plan) {
        service.savePlan(plan);
        return R.ok();
    }

    @PutMapping("/plan")
    public R<?> updatePlan(@RequestBody ScoringPlan plan) {
        service.savePlan(plan);
        return R.ok();
    }

    @DeleteMapping("/plan/{id}")
    public R<?> deletePlan(@PathVariable Long id) {
        service.deletePlan(id);
        return R.ok();
    }

    @PostMapping("/calculate/{planId}")
    public R<List<Map<String, Object>>> calculate(@PathVariable Long planId) {
        return R.ok(service.calculate(planId));
    }

    @GetMapping("/ranking/{planId}")
    public R<List<Map<String, Object>>> ranking(@PathVariable Long planId) {
        return R.ok(service.getRanking(planId));
    }
}
