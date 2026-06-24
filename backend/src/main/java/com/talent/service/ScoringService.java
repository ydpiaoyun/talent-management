package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talent.common.ExpressionEvaluator;
import com.talent.entity.*;
import com.talent.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评分服务
 * <p>
 * 基于 Aviator 表达式引擎执行人才评分计算，支持自定义公式和加权和回退。
 * 所有数据库操作使用 MyBatis-Plus 代码方式，不写 SQL。
 * </p>
 *
 * @author talent-hr
 */
@Service
public class ScoringService {

    private static final Logger log = LoggerFactory.getLogger(ScoringService.class);

    private final ScoringPlanMapper planMapper;
    private final TalentScoreMapper scoreMapper;
    private final TalentAttributeMapper attrMapper;
    private final TalentAttrValueService attrValueService;
    private final TalentMapper talentMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造方法
     *
     * @param planMapper       评分方案 Mapper
     * @param scoreMapper      评分记录 Mapper
     * @param attrMapper       指标 Mapper
     * @param attrValueService 指标值 Service
     * @param talentMapper     人才 Mapper
     */
    public ScoringService(ScoringPlanMapper planMapper, TalentScoreMapper scoreMapper,
                          TalentAttributeMapper attrMapper, TalentAttrValueService attrValueService,
                          TalentMapper talentMapper) {
        this.planMapper = planMapper;
        this.scoreMapper = scoreMapper;
        this.attrMapper = attrMapper;
        this.attrValueService = attrValueService;
        this.talentMapper = talentMapper;
    }

    /**
     * 执行评分计算 — 使用 Aviator 表达式引擎
     * <p>
     * 表达式求值失败时自动回退到加权和算法。
     * </p>
     *
     * @param planId 评分方案 ID
     * @return 评分结果列表（按总分降序排列，含排名）
     */
    @Transactional
    @CacheEvict(value = "scoringRanking", key = "#planId")
    public List<Map<String, Object>> calculate(Long planId) {
        ScoringPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            return Collections.emptyList();
        }

        List<TalentAttribute> attrs = attrMapper.selectList(
                new LambdaQueryWrapper<TalentAttribute>().eq(TalentAttribute::getStatus, 1)
                        .orderByAsc(TalentAttribute::getSortOrder));

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
                String rawVal = attrValueService.findValueByTalentAndAttr(talent.getId(), attr.getId());
                double score = normalize(rawVal, attr);
                double rounded = Math.round(score * 100.0) / 100.0;
                detail.put(attr.getCode(), rounded);
                scoreVars.put(attr.getCode(), rounded);
            }

            // 2. 计算总分
            double totalScore;
            if (useAviator) {
                try {
                    totalScore = ExpressionEvaluator.eval(plan.getExpression(), scoreVars);
                } catch (Exception e) {
                    log.warn("表达式求值失败，回退到加权和算法: 人才={}, 错误={}", talent.getName(), e.getMessage());
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
            } catch (Exception e) {
                log.error("评分明细 JSON 序列化失败: 人才={}", talent.getName(), e);
            }
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

        // 按总分降序排列并生成排名
        results.sort((a, b) -> Double.compare(
                (Double) b.get("totalScore"), (Double) a.get("totalScore")));
        for (int i = 0; i < results.size(); i++) {
            results.get(i).put("rank", i + 1);
        }

        return results;
    }

    /**
     * 传统加权和算法（表达式求值失败时的回退方案）
     *
     * @param attrs     指标列表
     * @param scoreVars 指标得分变量
     * @return 加权总分
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
     * 指标值归一化到 0-100
     *
     * @param rawValue 原始值
     * @param attr     指标定义
     * @return 归一化后的分值（0-100）
     */
    private double normalize(String rawValue, TalentAttribute attr) {
        if (rawValue == null || rawValue.isBlank()) {
            return 0;
        }

        if ("ENUM".equals(attr.getType())) {
            Map<String, Integer> mapping = parseScoreMapping(attr.getScoreMapping());
            if (mapping != null && mapping.containsKey(rawValue)) {
                return mapping.get(rawValue);
            }
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
                double min = attr.getMinValue() != null ? attr.getMinValue() : 0;
                double max = attr.getMaxValue() != null ? attr.getMaxValue() : 100;
                if ("NUMBER".equals(attr.getType())) {
                    switch (attr.getCode()) {
                        case "work_years" -> { min = 0; max = 20; }
                        case "height" -> { min = 150; max = 190; }
                        case "age" -> { min = 20; max = 60; }
                        case "cert_count" -> { min = 0; max = 10; }
                        case "manage_years" -> { min = 0; max = 10; }
                    }
                }
                if (max <= min) {
                    return 0;
                }
                double normalized = Math.max(0, Math.min(100, (val - min) / (max - min) * 100));
                if (attr.getDirection() != null && attr.getDirection() == -1) {
                    normalized = 100 - normalized;
                }
                return normalized;
            } catch (NumberFormatException e) {
                log.warn("指标值无法转为数字: code={}, value={}", attr.getCode(), rawValue);
                return 0;
            }
        }

        return 60;
    }

    /**
     * 解析分值映射 JSON
     *
     * @param json JSON 字符串
     * @return 指标值到分值的映射，解析失败返回 null
     */
    private Map<String, Integer> parseScoreMapping(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            log.warn("分值映射 JSON 解析失败: {}", json);
            return null;
        }
    }

    /**
     * 解析枚举选项 JSON
     *
     * @param json JSON 字符串
     * @return 选项列表，解析失败返回 null
     */
    private List<String> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("枚举选项 JSON 解析失败: {}", json);
            return null;
        }
    }

    /**
     * 获取评分排名
     * <p>
     * 使用 MyBatis-Plus 代码方式替代 SQL JOIN 查询
     * </p>
     *
     * @param planId 评分方案 ID
     * @return 排名列表（按总分降序）
     */
    @Cacheable(value = "scoringRanking", key = "#planId")
    public List<Map<String, Object>> getRanking(Long planId) {
        // 查询评分记录，按总分降序
        LambdaQueryWrapper<TalentScore> qw = new LambdaQueryWrapper<>();
        qw.eq(TalentScore::getPlanId, planId).orderByDesc(TalentScore::getTotalScore);
        List<TalentScore> scores = scoreMapper.selectList(qw);

        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询人才信息
        List<Long> talentIds = scores.stream()
                .map(TalentScore::getTalentId)
                .collect(Collectors.toList());
        Map<Long, Talent> talentMap = talentMapper.selectBatchIds(talentIds).stream()
                .collect(Collectors.toMap(Talent::getId, t -> t));

        // 组装结果
        List<Map<String, Object>> results = new ArrayList<>();
        for (TalentScore s : scores) {
            Talent t = talentMap.get(s.getTalentId());
            if (t == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("talentId", s.getTalentId());
            item.put("name", t.getName());
            item.put("dept", t.getDept());
            item.put("position", t.getPosition());
            item.put("totalScore", s.getTotalScore());
            item.put("detailJson", s.getDetailJson());
            results.add(item);
        }
        return results;
    }

    // --- 评分方案 CRUD ---

    /**
     * 查询所有评分方案
     *
     * @return 评分方案列表
     */
    public List<ScoringPlan> listPlans() {
        return planMapper.selectList(null);
    }

    /**
     * 根据 ID 查询评分方案
     *
     * @param id 方案 ID
     * @return 评分方案
     */
    public ScoringPlan getPlan(Long id) {
        return planMapper.selectById(id);
    }

    /**
     * 保存评分方案（新增或更新）
     *
     * @param plan 评分方案
     */
    public void savePlan(ScoringPlan plan) {
        if (plan.getId() == null) {
            planMapper.insert(plan);
        } else {
            planMapper.updateById(plan);
        }
    }

    /**
     * 删除评分方案（逻辑删除）
     *
     * @param id 方案 ID
     */
    public void deletePlan(Long id) {
        planMapper.deleteById(id);
    }
}
