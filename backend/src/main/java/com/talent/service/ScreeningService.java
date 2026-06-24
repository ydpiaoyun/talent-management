package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.entity.*;
import com.talent.mapper.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛选服务
 * <p>
 * 根据筛选方案中的条件组合（AND/OR），匹配满足条件的人才列表。
 * 所有数据库操作使用 MyBatis-Plus 代码方式，不写 SQL。
 * </p>
 *
 * @author talent-hr
 */
@Service
public class ScreeningService {

    private final ScreeningPlanMapper planMapper;
    private final SelectionConditionMapper condMapper;
    private final TalentMapper talentMapper;
    private final TalentAttrValueMapper valueMapper;
    private final TalentAttributeMapper attrMapper;

    /**
     * 构造方法
     *
     * @param planMapper   筛选方案 Mapper
     * @param condMapper   筛选条件 Mapper
     * @param talentMapper 人才 Mapper
     * @param valueMapper  指标值 Mapper
     * @param attrMapper   指标 Mapper
     */
    public ScreeningService(ScreeningPlanMapper planMapper, SelectionConditionMapper condMapper,
                            TalentMapper talentMapper, TalentAttrValueMapper valueMapper,
                            TalentAttributeMapper attrMapper) {
        this.planMapper = planMapper;
        this.condMapper = condMapper;
        this.talentMapper = talentMapper;
        this.valueMapper = valueMapper;
        this.attrMapper = attrMapper;
    }

    /**
     * 执行筛选方案，返回匹配的人才列表
     * <p>
     * 使用 MyBatis-Plus 代码方式替代 SQL 查询
     * </p>
     *
     * @param planId 筛选方案 ID
     * @return 匹配的人才列表
     */
    @Cacheable(value = "screeningResult", key = "#planId")
    public List<Map<String, Object>> execute(Long planId) {
        ScreeningPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            return Collections.emptyList();
        }

        List<SelectionCondition> conditions = condMapper.selectList(
                new LambdaQueryWrapper<SelectionCondition>()
                        .eq(SelectionCondition::getPlanId, planId)
                        .orderByAsc(SelectionCondition::getSortOrder));

        if (conditions.isEmpty()) {
            return Collections.emptyList();
        }

        // 加载所有指标信息
        Map<Long, TalentAttribute> attrMap = attrMapper.selectList(null).stream()
                .collect(Collectors.toMap(TalentAttribute::getId, a -> a));

        // 对每个条件，查询满足条件的人才 ID 集合
        List<Set<Long>> matchSets = new ArrayList<>();
        for (SelectionCondition cond : conditions) {
            Set<Long> matched = queryMatchedTalentIds(cond, attrMap.get(cond.getAttrId()));
            matchSets.add(matched);
        }

        // 根据逻辑类型取交集或并集
        Set<Long> resultIds;
        if ("OR".equalsIgnoreCase(plan.getLogicType())) {
            resultIds = new HashSet<>();
            for (Set<Long> s : matchSets) {
                resultIds.addAll(s);
            }
        } else {
            // AND：取交集
            resultIds = new HashSet<>(matchSets.get(0));
            for (int i = 1; i < matchSets.size(); i++) {
                resultIds.retainAll(matchSets.get(i));
            }
        }

        // 加载人才详情
        return buildResultList(resultIds);
    }

    /**
     * 根据单个条件查询匹配的人才 ID 集合
     * <p>
     * 使用 MyBatis-Plus 按 attrId 过滤，再在 Java 中匹配值
     * </p>
     *
     * @param cond 筛选条件
     * @param attr 对应指标定义
     * @return 匹配的人才 ID 集合
     */
    private Set<Long> queryMatchedTalentIds(SelectionCondition cond, TalentAttribute attr) {
        if (attr == null) {
            return Collections.emptySet();
        }

        String operator = cond.getOperator();
        String expected = cond.getValue();
        String type = attr.getType();

        // 按 attrId 过滤，只加载相关指标值
        LambdaQueryWrapper<TalentAttrValue> qw = new LambdaQueryWrapper<>();
        qw.eq(TalentAttrValue::getAttrId, cond.getAttrId());
        List<TalentAttrValue> attrValues = valueMapper.selectList(qw);

        Set<Long> result = new HashSet<>();
        for (TalentAttrValue v : attrValues) {
            if (matchValue(v.getValueText(), type, operator, expected)) {
                result.add(v.getTalentId());
            }
        }
        return result;
    }

    /**
     * 在 Java 中匹配指标值（替代 SQL 中的条件判断）
     *
     * @param actual   实际值
     * @param type     指标类型
     * @param operator 操作符
     * @param expected 期望值
     * @return true 匹配，false 不匹配
     */
    private boolean matchValue(String actual, String type, String operator, String expected) {
        if (actual == null) {
            actual = "";
        }
        switch (operator) {
            case "EQ":
                return actual.equals(expected);
            case "NE":
                return !actual.equals(expected);
            case "GT", "GTE", "LT", "LTE":
                try {
                    double a = Double.parseDouble(actual);
                    double e = Double.parseDouble(expected);
                    return switch (operator) {
                        case "GT" -> a > e;
                        case "GTE" -> a >= e;
                        case "LT" -> a < e;
                        case "LTE" -> a <= e;
                        default -> false;
                    };
                } catch (NumberFormatException ex) {
                    return false;
                }
            case "IN":
                return Arrays.asList(expected.split(",")).contains(actual);
            case "LIKE":
                return actual.contains(expected);
            case "NOT_LIKE":
                return !actual.contains(expected);
            default:
                return false;
        }
    }

    /**
     * 构建筛选结果列表
     *
     * @param talentIds 人才 ID 集合
     * @return 人才信息列表
     */
    private List<Map<String, Object>> buildResultList(Set<Long> talentIds) {
        if (talentIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Talent> talents = talentMapper.selectBatchIds(talentIds);
        Map<Long, Talent> talentMap = talents.stream()
                .collect(Collectors.toMap(Talent::getId, t -> t));
        return talentIds.stream()
                .map(talentMap::get)
                .filter(Objects::nonNull)
                .map(t -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", t.getId());
                    item.put("name", t.getName());
                    item.put("gender", t.getGender());
                    item.put("dept", t.getDept());
                    item.put("position", t.getPosition());
                    item.put("email", t.getEmail());
                    item.put("phone", t.getPhone());
                    return item;
                }).collect(Collectors.toList());
    }

    // --- 筛选方案 CRUD ---

    /**
     * 查询所有筛选方案
     *
     * @return 筛选方案列表
     */
    public List<ScreeningPlan> listPlans() {
        return planMapper.selectList(null);
    }

    /**
     * 根据 ID 查询筛选方案
     *
     * @param id 方案 ID
     * @return 筛选方案
     */
    public ScreeningPlan getPlan(Long id) {
        return planMapper.selectById(id);
    }

    /**
     * 保存筛选方案（新增或更新）
     *
     * @param plan 筛选方案
     */
    @CacheEvict(value = "screeningResult", allEntries = true)
    public void savePlan(ScreeningPlan plan) {
        if (plan.getId() == null) {
            planMapper.insert(plan);
        } else {
            planMapper.updateById(plan);
        }
    }

    /**
     * 删除筛选方案（逻辑删除）
     *
     * @param id 方案 ID
     */
    @CacheEvict(value = "screeningResult", allEntries = true)
    public void deletePlan(Long id) {
        planMapper.deleteById(id);
    }
}
