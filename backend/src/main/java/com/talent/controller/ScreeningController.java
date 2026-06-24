package com.talent.controller;

import com.talent.common.R;
import com.talent.entity.ScreeningPlan;
import com.talent.entity.SelectionCondition;
import com.talent.service.ScreeningService;
import com.talent.service.SelectionConditionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 筛选管理接口
 * <p>
 * 提供筛选方案的增删改查、条件管理及执行筛选功能。
 * </p>
 *
 * @author talent-hr
 */
@RestController
@RequestMapping("/api/screening")
public class ScreeningController {

    private final ScreeningService screeningService;
    private final SelectionConditionService condService;

    /**
     * 构造方法
     *
     * @param screeningService 筛选服务
     * @param condService      条件服务
     */
    public ScreeningController(ScreeningService screeningService,
                               SelectionConditionService condService) {
        this.screeningService = screeningService;
        this.condService = condService;
    }

    // --- 筛选方案 ---

    /**
     * 查询所有筛选方案
     *
     * @return 方案列表
     */
    @GetMapping("/plan/list")
    public R<List<ScreeningPlan>> listPlans() {
        return R.ok(screeningService.listPlans());
    }

    /**
     * 根据 ID 查询筛选方案
     *
     * @param id 方案 ID
     * @return 方案详情
     */
    @GetMapping("/plan/{id}")
    public R<ScreeningPlan> getPlan(@PathVariable Long id) {
        return R.ok(screeningService.getPlan(id));
    }

    /**
     * 新增筛选方案
     *
     * @param plan 方案实体
     * @return 操作结果
     */
    @PostMapping("/plan")
    public R<?> savePlan(@RequestBody ScreeningPlan plan) {
        screeningService.savePlan(plan);
        return R.ok();
    }

    /**
     * 更新筛选方案
     *
     * @param plan 方案实体
     * @return 操作结果
     */
    @PutMapping("/plan")
    public R<?> updatePlan(@RequestBody ScreeningPlan plan) {
        screeningService.savePlan(plan);
        return R.ok();
    }

    /**
     * 删除筛选方案（逻辑删除）
     *
     * @param id 方案 ID
     * @return 操作结果
     */
    @DeleteMapping("/plan/{id}")
    public R<?> deletePlan(@PathVariable Long id) {
        screeningService.deletePlan(id);
        return R.ok();
    }

    // --- 筛选条件 ---

    /**
     * 查询指定方案的筛选条件列表
     *
     * @param planId 方案 ID
     * @return 条件列表
     */
    @GetMapping("/condition/list/{planId}")
    public R<List<SelectionCondition>> listConditions(@PathVariable Long planId) {
        return R.ok(condService.listByPlanId(planId));
    }

    /**
     * 新增筛选条件
     *
     * @param condition 条件实体
     * @return 操作结果
     */
    @PostMapping("/condition")
    public R<?> saveCondition(@RequestBody SelectionCondition condition) {
        condService.save(condition);
        return R.ok();
    }

    /**
     * 更新筛选条件
     *
     * @param condition 条件实体
     * @return 操作结果
     */
    @PutMapping("/condition")
    public R<?> updateCondition(@RequestBody SelectionCondition condition) {
        condService.save(condition);
        return R.ok();
    }

    /**
     * 删除筛选条件（逻辑删除）
     *
     * @param id 条件 ID
     * @return 操作结果
     */
    @DeleteMapping("/condition/{id}")
    public R<?> deleteCondition(@PathVariable Long id) {
        condService.delete(id);
        return R.ok();
    }

    /**
     * 批量保存筛选条件（先删后插）
     *
     * @param planId     方案 ID
     * @param conditions 条件列表
     * @return 操作结果
     */
    @PostMapping("/condition/batch/{planId}")
    public R<?> batchSave(@PathVariable Long planId, @RequestBody List<SelectionCondition> conditions) {
        condService.batchSave(planId, conditions);
        return R.ok();
    }

    // --- 执行筛选 ---

    /**
     * 执行筛选方案，返回匹配的人才列表
     *
     * @param planId 方案 ID
     * @return 匹配的人才列表
     */
    @PostMapping("/execute/{planId}")
    public R<List<Map<String, Object>>> execute(@PathVariable Long planId) {
        return R.ok(screeningService.execute(planId));
    }
}
