package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.entity.TalentAttrValue;
import com.talent.mapper.TalentAttrValueMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 人才指标值服务
 * <p>
 * 封装所有对 talent_attr_value 表的复杂操作，使用 MyBatis-Plus 代码方式。
 * </p>
 *
 * @author talent-hr
 */
@Service
public class TalentAttrValueService {

    private final TalentAttrValueMapper valueMapper;

    /**
     * 构造方法
     *
     * @param valueMapper 指标值 Mapper
     */
    public TalentAttrValueService(TalentAttrValueMapper valueMapper) {
        this.valueMapper = valueMapper;
    }

    /**
     * 根据人才 ID 和指标 ID 查找指标值
     *
     * @param talentId 人才 ID
     * @param attrId   指标 ID
     * @return 指标值文本，未找到返回 null
     */
    public String findValueByTalentAndAttr(Long talentId, Long attrId) {
        LambdaQueryWrapper<TalentAttrValue> qw = new LambdaQueryWrapper<>();
        qw.eq(TalentAttrValue::getTalentId, talentId)
          .eq(TalentAttrValue::getAttrId, attrId)
          .last("LIMIT 1");
        TalentAttrValue v = valueMapper.selectOne(qw);
        return v == null ? null : v.getValueText();
    }

    /**
     * 批量查询多个人才的指标值（含指标 code）
     * <p>
     * 替代原 batchFindByTalentIds SQL JOIN 查询，改为 MyBatis-Plus + Java 组装
     * </p>
     *
     * @param talentIds    人才 ID 列表
     * @param attrIdToCode 指标 ID 到 code 的映射
     * @return 指标值列表（每项含 TALENT_ID、ATTR_ID、VALUE_TEXT、ATTR_CODE）
     */
    public List<Map<String, Object>> batchFindByTalentIds(List<Long> talentIds, Map<Long, String> attrIdToCode) {
        if (talentIds == null || talentIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        LambdaQueryWrapper<TalentAttrValue> qw = new LambdaQueryWrapper<>();
        qw.in(TalentAttrValue::getTalentId, talentIds);
        List<TalentAttrValue> rows = valueMapper.selectList(qw);

        return rows.stream().map(v -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("TALENT_ID", v.getTalentId());
            map.put("ATTR_ID", v.getAttrId());
            map.put("VALUE_TEXT", v.getValueText());
            map.put("ATTR_CODE", attrIdToCode.getOrDefault(v.getAttrId(), ""));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 删除指定人才和指标的值
     *
     * @param talentId 人才 ID
     * @param attrId   指标 ID
     */
    public void deleteByTalentAndAttr(Long talentId, Long attrId) {
        LambdaQueryWrapper<TalentAttrValue> qw = new LambdaQueryWrapper<>();
        qw.eq(TalentAttrValue::getTalentId, talentId)
          .eq(TalentAttrValue::getAttrId, attrId);
        valueMapper.delete(qw);
    }

    /**
     * 批量保存人才指标值（先删后插）
     *
     * @param talentId   人才 ID
     * @param attrValues 指标值 Map（attrId -> value）
     */
    public void saveAttrValues(Long talentId, java.util.Map<Long, String> attrValues) {
        if (attrValues == null || attrValues.isEmpty()) {
            return;
        }
        for (java.util.Map.Entry<Long, String> entry : attrValues.entrySet()) {
            // 先删旧值
            LambdaQueryWrapper<TalentAttrValue> qw = new LambdaQueryWrapper<>();
            qw.eq(TalentAttrValue::getTalentId, talentId)
              .eq(TalentAttrValue::getAttrId, entry.getKey());
            valueMapper.delete(qw);
            // 插入新值
            TalentAttrValue v = new TalentAttrValue();
            v.setTalentId(talentId);
            v.setAttrId(entry.getKey());
            v.setValueText(entry.getValue());
            valueMapper.insert(v);
        }
    }

    /**
     * 查询指定人才的所有指标值，返回 attrCode -> value 的 Map
     *
     * @param talentId     人才 ID
     * @param attrIdToCode 指标 ID 到 code 的映射
     * @return 指标 code 到值的映射
     */
    public java.util.Map<String, String> findValuesMap(Long talentId, java.util.Map<Long, String> attrIdToCode) {
        LambdaQueryWrapper<TalentAttrValue> qw = new LambdaQueryWrapper<>();
        qw.eq(TalentAttrValue::getTalentId, talentId);
        List<TalentAttrValue> rows = valueMapper.selectList(qw);
        java.util.Map<String, String> result = new java.util.HashMap<>();
        for (TalentAttrValue v : rows) {
            String code = attrIdToCode.getOrDefault(v.getAttrId(), v.getAttrId().toString());
            result.put(code, v.getValueText());
        }
        return result;
    }
}
