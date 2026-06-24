package com.talent.controller;

import com.talent.common.R;
import com.talent.entity.ScoringPlan;
import com.talent.service.ScoringService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评分管理接口
 * <p>
 * 提供评分方案的增删改查、执行评分计算及排名查询功能。
 * </p>
 *
 * @author talent-hr
 */
@RestController
@RequestMapping("/api/scoring")
public class ScoringController {

    private final ScoringService service;

    /**
     * 构造方法
     *
     * @param service 评分服务
     */
    public ScoringController(ScoringService service) {
        this.service = service;
    }

    /**
     * 查询所有评分方案
     *
     * @return 方案列表
     */
    @GetMapping("/plan/list")
    public R<List<ScoringPlan>> listPlans() {
        return R.ok(service.listPlans());
    }

    /**
     * 根据 ID 查询评分方案
     *
     * @param id 方案 ID
     * @return 方案详情
     */
    @GetMapping("/plan/{id}")
    public R<ScoringPlan> getPlan(@PathVariable Long id) {
        return R.ok(service.getPlan(id));
    }

    /**
     * 新增评分方案
     *
     * @param plan 方案实体
     * @return 操作结果
     */
    @PostMapping("/plan")
    public R<?> savePlan(@RequestBody ScoringPlan plan) {
        service.savePlan(plan);
        return R.ok();
    }

    /**
     * 更新评分方案
     *
     * @param plan 方案实体
     * @return 操作结果
     */
    @PutMapping("/plan")
    public R<?> updatePlan(@RequestBody ScoringPlan plan) {
        service.savePlan(plan);
        return R.ok();
    }

    /**
     * 删除评分方案（逻辑删除）
     *
     * @param id 方案 ID
     * @return 操作结果
     */
    @DeleteMapping("/plan/{id}")
    public R<?> deletePlan(@PathVariable Long id) {
        service.deletePlan(id);
        return R.ok();
    }

    /**
     * 执行评分计算，返回排名结果
     *
     * @param planId 方案 ID
     * @return 评分排名列表
     */
    @PostMapping("/calculate/{planId}")
    public R<List<Map<String, Object>>> calculate(@PathVariable Long planId) {
        return R.ok(service.calculate(planId));
    }

    /**
     * 查询评分排名（从缓存或数据库）
     *
     * @param planId 方案 ID
     * @return 排名列表
     */
    @GetMapping("/ranking/{planId}")
    public R<List<Map<String, Object>>> ranking(@PathVariable Long planId) {
        return R.ok(service.getRanking(planId));
    }
}
