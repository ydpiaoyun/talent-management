package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talent.common.ExpressionEvaluator;
import com.talent.entity.*;
import com.talent.mapper.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoringService {

    private final ScoringPlanMapper planMapper;
    private final TalentScoreMapper scoreMapper;
    private final TalentAttributeMapper attrMapper;
    private final TalentAttrValueMapper valueMapper;
    private final TalentMapper talentMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScoringService(ScoringPlanMapper planMapper, TalentScoreMapper scoreMapper,
                          TalentAttributeMapper attrMapper, TalentAttrValueMapper valueMapper,
                          TalentMapper talentMapper) {
        this.planMapper = planMapper;
        this.scoreMapper = scoreMapper;
        this.attrMapper = attrMapper;
        this.valueMapper = valueMapper;
        this.talentMapper = talentMapper;
    }

    /**
     * 执行评分计算 — 使用 Aviator 表达式引擎
     * <p>评分方案若配置了 expression 字段，则用 Aviator 动态求值；
     * 否则回退到传统的权重加权和算法。
     */
    @Transactional
    @CacheEvict(value = "scoringRanking", key = "#planId")
    public List<Map<String, Object>> calculate(Long planId) {
        ScoringPlan plan = planMapper.selectById(planId);
        if (plan == null) return Collections.emptyList();

        // 获取所有启用的指标
        List<TalentAttribute> attrs = attrMapper.selectList(
                new LambdaQueryWrapper<TalentAttribute>().eq(TalentAttribute::getStatus, 1)
                        .orderByAsc(TalentAttribute::getSortOrder));

        // 获取所有人才
        List<Talent> talents = talentMapper.selectList(null);

        // 清除旧评分
        scoreMapper.delete(new LambdaQueryWrapper<TalentScore>()
                .eq(TalentScore::getPlanId, planId));

        boolean useAviator = plan.getExpression() != null && !plan.getExpression().isBlank();

        List<Map<String, Object>> results = new ArrayList<>();

        for (Talent talent : talents) {
            Map<String, Object> detail = new LinkedHashMap<>();

            // 1. 归一化所有指标到 0-100
            Map<String, Object> scoreVars = new LinkedHashMap<>();
            for (TalentAttribute attr : attrs) {
                String rawVal = valueMapper.findValueByTalentAndAttr(talent.getId(), attr.getId());
                double score = normalize(rawVal, attr);
                double rounded = Math.round(score * 100.0) / 100.0;
                detail.put(attr.getCode(), rounded);
                scoreVars.put(attr.getCode(), rounded);
            }

            // 2. 计算总分
            double totalScore;
            if (useAviator) {
                // 使用 Aviator 表达式求值
                try {
                    totalScore = ExpressionEvaluator.eval(plan.getExpression(), scoreVars);
                } catch (Exception e) {
                    // 表达式求值失败时，回退到加权和
                    totalScore = computeWeightedSum(attrs, scoreVars);
                }
            } else {
                totalScore = computeWeightedSum(attrs, scoreVars);
            }
            totalScore = Math.round(totalScore * 100.0) / 100.0;

            // 3. 保存评分
            TalentScore ts = new TalentScore();
            ts.setTalentId(talent.getId());
            ts.setPlanId(planId);
            ts.setTotalScore(totalScore);
            try {
                ts.setDetailJson(objectMapper.writeValueAsString(detail));
            } catch (Exception ignored) {}
            scoreMapper.insert(ts);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("talentId", talent.getId());
            item.put("name", talent.getName());
            item.put("dept", talent.getDept());
            item.put("position", talent.getPosition());
            item.put("totalScore", totalScore);
            item.put("detail", detail);
            results.add(item);
        }

        // 按总分降序 → 排名
        results.sort((a, b) -> Double.compare(
                (Double) b.get("totalScore"), (Double) a.get("totalScore")));
        for (int i = 0; i < results.size(); i++) {
            results.get(i).put("rank", i + 1);
        }

        return results;
    }

    /**
     * 传统加权和算法（回退方案）
     */
    private double computeWeightedSum(List<TalentAttribute> attrs, Map<String, Object> scoreVars) {
        double totalScore = 0, totalWeight = 0;
        for (TalentAttribute attr : attrs) {
            if (attr.getWeight() != null && attr.getWeight() > 0) {
                double s = ((Number) scoreVars.getOrDefault(attr.getCode(), 0.0)).doubleValue();
                totalScore += s * attr.getWeight() / 100.0;
                totalWeight += attr.getWeight();
            }
        }
        return totalWeight > 0 ? totalScore / (totalWeight / 100.0) : 0;
    }

    /**
     * 指标值归一化到0-100
     */
    private double normalize(String rawValue, TalentAttribute attr) {
        if (rawValue == null || rawValue.isBlank()) return 0;

        if ("ENUM".equals(attr.getType())) {
            // 使用分值映射
            Map<String, Integer> mapping = parseScoreMapping(attr.getScoreMapping());
            if (mapping != null && mapping.containsKey(rawValue)) {
                return mapping.get(rawValue);
            }
            // 默认：第一个选项100，最后0
            List<String> options = parseOptions(attr.getOptionsJson());
            if (options != null && !options.isEmpty()) {
                int idx = options.indexOf(rawValue);
                if (idx >= 0) {
                    return 100 - (idx * 100 / (options.size() - 1));
                }
            }
            return 0;
        }

        if ("NUMBER".equals(attr.getType()) || "RANGE".equals(attr.getType())) {
            try {
                double val = Double.parseDouble(rawValue);
                // 范围类型使用指标配置的最小值/最大值；NUMBER 类型用内置范围
                double min = attr.getMinValue() != null ? attr.getMinValue() : 0;
                double max = attr.getMaxValue() != null ? attr.getMaxValue() : 100;
                if ("NUMBER".equals(attr.getType())) {
                    // NUMBER 类型沿用原有内置范围
                    switch (attr.getCode()) {
                        case "work_years": min = 0; max = 20; break;
                        case "height": min = 150; max = 190; break;
                        case "age": min = 20; max = 60; break;
                        case "cert_count": min = 0; max = 10; break;
                        case "manage_years": min = 0; max = 10; break;
                    }
                }
                if (max <= min) return 0; // 防止除零
                double normalized = Math.max(0, Math.min(100, (val - min) / (max - min) * 100));
                // 逆指标反转
                if (attr.getDirection() != null && attr.getDirection() == -1) {
                    normalized = 100 - normalized;
                }
                return normalized;
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        // TEXT类型有值就给60分
        return rawValue.isBlank() ? 0 : 60;
    }

    private Map<String, Integer> parseScoreMapping(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseOptions(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取评分排名
     */
    @Cacheable(value = "scoringRanking", key = "#planId")
    public List<Map<String, Object>> getRanking(Long planId) {
        return scoreMapper.rankingByPlan(planId);
    }

    // --- ScoringPlan CRUD ---
    public List<ScoringPlan> listPlans() {
        return planMapper.selectList(null);
    }

    public ScoringPlan getPlan(Long id) {
        return planMapper.selectById(id);
    }

    public void savePlan(ScoringPlan plan) {
        if (plan.getId() == null) {
            planMapper.insert(plan);
        } else {
            planMapper.updateById(plan);
        }
    }

    public void deletePlan(Long id) {
        planMapper.deleteById(id);
    }
}
