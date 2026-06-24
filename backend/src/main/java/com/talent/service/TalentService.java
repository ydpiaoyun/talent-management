package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.talent.entity.*;
import com.talent.mapper.TalentAttributeMapper;
import com.talent.mapper.TalentMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 人才服务
 * <p>
 * 封装人才信息及指标值的查询、保存、删除等业务逻辑。
 * 所有数据库操作使用 MyBatis-Plus 代码方式，不写 SQL。
 * </p>
 *
 * @author talent-hr
 */
@Service
public class TalentService {

    private final TalentMapper talentMapper;
    private final TalentAttrValueService attrValueService;
    private final TalentAttributeMapper attrMapper;

    /**
     * 构造方法
     *
     * @param talentMapper     人才 Mapper
     * @param attrValueService 指标值 Service
     * @param attrMapper       指标 Mapper
     */
    public TalentService(TalentMapper talentMapper,
                         TalentAttrValueService attrValueService,
                         TalentAttributeMapper attrMapper) {
        this.talentMapper = talentMapper;
        this.attrValueService = attrValueService;
        this.attrMapper = attrMapper;
    }

    /**
     * 分页查询人才列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  搜索关键词（姓名或部门）
     * @return 分页结果
     */
    public Page<Talent> page(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<Talent> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.like(Talent::getName, keyword).or().like(Talent::getDept, keyword);
        }
        qw.orderByDesc(Talent::getCreateTime);
        return talentMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    /**
     * 根据 ID 查询人才
     *
     * @param id 人才 ID
     * @return 人才实体
     */
    public Talent getById(Long id) {
        return talentMapper.selectById(id);
    }

    /**
     * 保存人才信息（新增或更新）及指标值
     *
     * @param talent     人才实体
     * @param attrValues 指标值 Map（attrId -> value）
     */
    @Transactional
    @CacheEvict(value = {"talentList", "talentDetail"}, allEntries = true)
    public void save(Talent talent, Map<Long, String> attrValues) {
        if (talent.getId() == null) {
            talentMapper.insert(talent);
        } else {
            talentMapper.updateById(talent);
        }
        // 保存指标值（先删后插）
        attrValueService.saveAttrValues(talent.getId(), attrValues);
    }

    /**
     * 删除人才（逻辑删除）
     *
     * @param id 人才 ID
     */
    @CacheEvict(value = {"talentList", "talentDetail"}, allEntries = true)
    public void delete(Long id) {
        talentMapper.deleteById(id);
    }

    /**
     * 获取人才的完整信息（含所有指标值）
     *
     * @param id 人才 ID
     * @return 人才详情 Map
     */
    @Cacheable(value = "talentDetail", key = "#id")
    public Map<String, Object> getDetail(Long id) {
        Talent talent = talentMapper.selectById(id);
        if (talent == null) {
            return null;
        }

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

            String val = attrValueService.findValueByTalentAndAttr(id, attr.getId());
            av.put("value", val);
            attrValues.add(av);
        }
        result.put("attributes", attrValues);
        return result;
    }

    /**
     * 获取所有人才列表（含指标值），供前端表格展示
     * <p>
     * 使用 MyBatis-Plus 代码方式替代 SQL JOIN 查询
     * </p>
     *
     * @return 人才列表（每项含指标值）
     */
    @Cacheable(value = "talentList")
    public List<Map<String, Object>> listWithAttrs() {
        List<Talent> talents = talentMapper.selectList(null);

        List<TalentAttribute> attrs = attrMapper.selectList(
                new LambdaQueryWrapper<TalentAttribute>().eq(TalentAttribute::getStatus, 1)
                        .orderByAsc(TalentAttribute::getSortOrder));

        // 构建 attrId -> code 映射
        Map<Long, String> attrIdToCode = attrs.stream()
                .collect(Collectors.toMap(TalentAttribute::getId, TalentAttribute::getCode));

        // 批量查询所有指标值（替代 SQL JOIN）
        List<Map<String, Object>> rawValues;
        if (!talents.isEmpty()) {
            List<Long> talentIds = talents.stream().map(Talent::getId).collect(Collectors.toList());
            rawValues = attrValueService.batchFindByTalentIds(talentIds, attrIdToCode);
        } else {
            rawValues = Collections.emptyList();
        }

        // 构建 talentId -> (attrCode -> value) 映射
        Map<Long, Map<String, String>> valueMap = new HashMap<>();
        for (Map<String, Object> row : rawValues) {
            Object tidObj = row.get("TALENT_ID");
            Object codeObj = row.get("ATTR_CODE");
            if (tidObj == null || codeObj == null) {
                continue;
            }
            Long tid = Long.valueOf(tidObj.toString());
            String code = codeObj.toString();
            String val = (String) row.getOrDefault("VALUE_TEXT", "");
            valueMap.computeIfAbsent(tid, k -> new HashMap<>()).put(code, val);
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

            Map<String, String> vals = valueMap.getOrDefault(t.getId(), Collections.emptyMap());
            for (TalentAttribute attr : attrs) {
                item.put(attr.getCode(), vals.get(attr.getCode()));
            }
            result.add(item);
        }
        return result;
    }
}
