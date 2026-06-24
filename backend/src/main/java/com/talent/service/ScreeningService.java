package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.entity.*;
import com.talent.mapper.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScreeningService {

    private final ScreeningPlanMapper planMapper;
    private final SelectionConditionMapper condMapper;
    private final TalentMapper talentMapper;
    private final TalentAttrValueMapper valueMapper;
    private final TalentAttributeMapper attrMapper;

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
     */
    @Cacheable(value = "screeningResult", key = "#planId")
    public List<Map<String, Object>> execute(Long planId) {
        ScreeningPlan plan = planMapper.selectById(planId);
        if (plan == null) return Collections.emptyList();

        List<SelectionCondition> conditions = condMapper.selectList(
                new LambdaQueryWrapper<SelectionCondition>()
                        .eq(SelectionCondition::getPlanId, planId)
                        .orderByAsc(SelectionCondition::getSortOrder));

        if (conditions.isEmpty()) return Collections.emptyList();

        // 加载所有指标信息
        Map<Long, TalentAttribute> attrMap = attrMapper.selectList(null).stream()
                .collect(Collectors.toMap(TalentAttribute::getId, a -> a));

        // 对每个条件，查询满足条件的人才ID集合
        List<Set<Long>> matchSets = new ArrayList<>();
        for (SelectionCondition cond : conditions) {
            Set<Long> matched = queryMatchedTalentIds(cond, attrMap.get(cond.getAttrId()));
            matchSets.add(matched);
        }

        // 根据逻辑类型取交集或并集
        Set<Long> resultIds;
        if ("OR".equalsIgnoreCase(plan.getLogicType())) {
            resultIds = new HashSet<>();
            for (Set<Long> s : matchSets) resultIds.addAll(s);
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

    private Set<Long> queryMatchedTalentIds(SelectionCondition cond, TalentAttribute attr) {
        if (attr == null) return Collections.emptySet();

        String operator = cond.getOperator();
        String value = cond.getValue();
        String type = attr.getType();

        // 对于ENUM类型，EQ和IN需要特殊处理
        // 对于NUMBER类型，需要进行数值比较
        List<com.talent.entity.TalentAttrValue> allValues = valueMapper.selectList(null);

        Set<Long> result = new HashSet<>();
        for (TalentAttrValue v : allValues) {
            if (!v.getAttrId().equals(cond.getAttrId())) continue;
            if (matchValue(v.getValueText(), type, operator, value)) {
                result.add(v.getTalentId());
            }
        }
        return result;
    }

    private boolean matchValue(String actual, String type, String operator, String expected) {
        if (actual == null) actual = "";
        switch (operator) {
            case "EQ":
                return actual.equals(expected);
            case "NE":
                return !actual.equals(expected);
            case "GT":
            case "GTE":
            case "LT":
            case "LTE":
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

    private List<Map<String, Object>> buildResultList(Set<Long> talentIds) {
        List<Talent> talents = talentMapper.selectBatchIds(talentIds);
        return talents.stream().map(t -> {
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

    // --- ScreeningPlan CRUD ---
    public List<ScreeningPlan> listPlans() {
        return planMapper.selectList(null);
    }

    public ScreeningPlan getPlan(Long id) {
        return planMapper.selectById(id);
    }

    @CacheEvict(value = "screeningResult", allEntries = true)
    public void savePlan(ScreeningPlan plan) {
        if (plan.getId() == null) {
            planMapper.insert(plan);
        } else {
            planMapper.updateById(plan);
        }
    }

    @CacheEvict(value = "screeningResult", allEntries = true)
    public void deletePlan(Long id) {
        planMapper.deleteById(id);
    }
}
