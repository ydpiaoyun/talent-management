package com.talent.controller;

import com.talent.common.R;
import com.talent.entity.ScreeningPlan;
import com.talent.entity.SelectionCondition;
import com.talent.service.ScreeningService;
import com.talent.service.SelectionConditionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/screening")
public class ScreeningController {

    private final ScreeningService screeningService;
    private final SelectionConditionService condService;

    public ScreeningController(ScreeningService screeningService,
                               SelectionConditionService condService) {
        this.screeningService = screeningService;
        this.condService = condService;
    }

    // --- 筛选方案 ---
    @GetMapping("/plan/list")
    public R<List<ScreeningPlan>> listPlans() {
        return R.ok(screeningService.listPlans());
    }

    @GetMapping("/plan/{id}")
    public R<ScreeningPlan> getPlan(@PathVariable Long id) {
        return R.ok(screeningService.getPlan(id));
    }

    @PostMapping("/plan")
    public R<?> savePlan(@RequestBody ScreeningPlan plan) {
        screeningService.savePlan(plan);
        return R.ok();
    }

    @PutMapping("/plan")
    public R<?> updatePlan(@RequestBody ScreeningPlan plan) {
        screeningService.savePlan(plan);
        return R.ok();
    }

    @DeleteMapping("/plan/{id}")
    public R<?> deletePlan(@PathVariable Long id) {
        screeningService.deletePlan(id);
        return R.ok();
    }

    // --- 条件 ---
    @GetMapping("/condition/list/{planId}")
    public R<List<SelectionCondition>> listConditions(@PathVariable Long planId) {
        return R.ok(condService.listByPlanId(planId));
    }

    @PostMapping("/condition")
    public R<?> saveCondition(@RequestBody SelectionCondition condition) {
        condService.save(condition);
        return R.ok();
    }

    @PutMapping("/condition")
    public R<?> updateCondition(@RequestBody SelectionCondition condition) {
        condService.save(condition);
        return R.ok();
    }

    @DeleteMapping("/condition/{id}")
    public R<?> deleteCondition(@PathVariable Long id) {
        condService.delete(id);
        return R.ok();
    }

    @PostMapping("/condition/batch/{planId}")
    public R<?> batchSave(@PathVariable Long planId, @RequestBody List<SelectionCondition> conditions) {
        condService.batchSave(planId, conditions);
        return R.ok();
    }

    // --- 执行筛选 ---
    @PostMapping("/execute/{planId}")
    public R<List<Map<String, Object>>> execute(@PathVariable Long planId) {
        return R.ok(screeningService.execute(planId));
    }
}
