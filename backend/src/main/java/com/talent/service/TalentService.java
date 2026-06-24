package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.talent.entity.*;
import com.talent.mapper.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TalentService {

    private final TalentMapper talentMapper;
    private final TalentAttrValueMapper valueMapper;
    private final TalentAttributeMapper attrMapper;

    public TalentService(TalentMapper talentMapper, TalentAttrValueMapper valueMapper,
                         TalentAttributeMapper attrMapper) {
        this.talentMapper = talentMapper;
        this.valueMapper = valueMapper;
        this.attrMapper = attrMapper;
    }

    public Page<Talent> page(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<Talent> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Talent::getName, keyword).or().like(Talent::getDept, keyword);
        }
        qw.orderByDesc(Talent::getCreateTime);
        return talentMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    public Talent getById(Long id) {
        return talentMapper.selectById(id);
    }

    @Transactional
    @CacheEvict(value = {"talentList", "talentDetail"}, allEntries = true)
    public void save(Talent talent, Map<Long, String> attrValues) {
        if (talent.getId() == null) {
            talentMapper.insert(talent);
        } else {
            talentMapper.updateById(talent);
        }
        // 保存指标值
        if (attrValues != null) {
            for (Map.Entry<Long, String> entry : attrValues.entrySet()) {
                // 先删除旧值
                valueMapper.delete(new LambdaQueryWrapper<TalentAttrValue>()
                        .eq(TalentAttrValue::getTalentId, talent.getId())
                        .eq(TalentAttrValue::getAttrId, entry.getKey()));
                // 插入新值
                TalentAttrValue v = new TalentAttrValue();
                v.setTalentId(talent.getId());
                v.setAttrId(entry.getKey());
                v.setValueText(entry.getValue());
                valueMapper.insert(v);
            }
        }
    }

    @CacheEvict(value = {"talentList", "talentDetail"}, allEntries = true)
    public void delete(Long id) {
        talentMapper.deleteById(id);
    }

    /**
     * 获取人才的完整信息（含所有指标值）
     */
    @Cacheable(value = "talentDetail", key = "#id")
    public Map<String, Object> getDetail(Long id) {
        Talent talent = talentMapper.selectById(id);
        if (talent == null) return null;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", talent.getId());
        result.put("name", talent.getName());
        result.put("gender", talent.getGender());
        result.put("birthDate", talent.getBirthDate());
        result.put("dept", talent.getDept());
        result.put("position", talent.getPosition());
        result.put("email", talent.getEmail());
        result.put("phone", talent.getPhone());
        result.put("remark", talent.getRemark());
        result.put("status", talent.getStatus());

        List<TalentAttribute> attrs = attrMapper.selectList(
                new LambdaQueryWrapper<TalentAttribute>().eq(TalentAttribute::getStatus, 1)
                        .orderByAsc(TalentAttribute::getSortOrder));

        List<Map<String, Object>> attrValues = new ArrayList<>();
        for (TalentAttribute attr : attrs) {
            Map<String, Object> av = new LinkedHashMap<>();
            av.put("attrId", attr.getId());
            av.put("code", attr.getCode());
            av.put("name", attr.getName());
            av.put("type", attr.getType());
            av.put("unit", attr.getUnit());
            av.put("weight", attr.getWeight());
            av.put("direction", attr.getDirection());

            String val = valueMapper.findValueByTalentAndAttr(id, attr.getId());
            av.put("value", val);
            attrValues.add(av);
        }
        result.put("attributes", attrValues);
        return result;
    }

    /**
     * 获取所有人才列表（含指标值），供前端表格展示
     */
    @Cacheable(value = "talentList")
    public List<Map<String, Object>> listWithAttrs() {
        List<Talent> talents = talentMapper.selectList(null);
        List<TalentAttribute> attrs = attrMapper.selectList(
                new LambdaQueryWrapper<TalentAttribute>().eq(TalentAttribute::getStatus, 1)
                        .orderByAsc(TalentAttribute::getSortOrder));

        String talentIds = talents.stream().map(t -> String.valueOf(t.getId()))
                .collect(Collectors.joining(","));

        List<Map<String, Object>> rawValues;
        if (!talents.isEmpty()) {
            rawValues = valueMapper.batchFindByTalentIds(talentIds);
        } else {
            rawValues = Collections.emptyList();
        }

        // 构建 talentId -> (attrId -> value) 映射
        Map<Long, Map<Long, String>> valueMap = new HashMap<>();
        for (Map<String, Object> row : rawValues) {
            Object tidObj = row.get("TALENT_ID");
            Object aidObj = row.get("ATTR_ID");
            if (tidObj == null || aidObj == null) continue;
            Long tid = Long.valueOf(tidObj.toString());
            Long aid = Long.valueOf(aidObj.toString());
            String val = (String) row.getOrDefault("VALUE_TEXT", "");
            valueMap.computeIfAbsent(tid, k -> new HashMap<>()).put(aid, val);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Talent t : talents) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", t.getId());
            item.put("name", t.getName());
            item.put("gender", t.getGender());
            item.put("dept", t.getDept());
            item.put("position", t.getPosition());
            item.put("email", t.getEmail());
            item.put("phone", t.getPhone());

            Map<Long, String> vals = valueMap.getOrDefault(t.getId(), Collections.emptyMap());
            for (TalentAttribute attr : attrs) {
                item.put(attr.getCode(), vals.get(attr.getId()));
            }
            result.add(item);
        }
        return result;
    }
}
