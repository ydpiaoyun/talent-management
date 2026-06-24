package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.entity.SelectionCondition;
import com.talent.mapper.SelectionConditionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 筛选条件服务
 * <p>
 * 封装筛选条件的查询、保存、删除、批量保存等业务逻辑。
 * </p>
 *
 * @author talent-hr
 */
@Service
public class SelectionConditionService {

    private final SelectionConditionMapper mapper;

    /**
     * 构造方法
     *
     * @param mapper 筛选条件 Mapper
     */
    public SelectionConditionService(SelectionConditionMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 根据方案 ID 查询条件列表（按排序序号升序）
     *
     * @param planId 方案 ID
     * @return 条件列表
     */
    public List<SelectionCondition> listByPlanId(Long planId) {
        return mapper.selectList(new LambdaQueryWrapper<SelectionCondition>()
                .eq(SelectionCondition::getPlanId, planId)
                .orderByAsc(SelectionCondition::getSortOrder));
    }

    /**
     * 保存条件（新增或更新）
     *
     * @param condition 条件实体
     */
    @Transactional
    public void save(SelectionCondition condition) {
        if (condition.getId() == null) {
            mapper.insert(condition);
        } else {
            mapper.updateById(condition);
        }
    }

    /**
     * 删除条件（逻辑删除）
     *
     * @param id 条件 ID
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /**
     * 批量保存条件（先删后插）
     *
     * @param planId     方案 ID
     * @param conditions 条件列表
     */
    @Transactional
    public void batchSave(Long planId, List<SelectionCondition> conditions) {
        // 删除旧条件
        mapper.delete(new LambdaQueryWrapper<SelectionCondition>()
                .eq(SelectionCondition::getPlanId, planId));
        // 插入新条件
        for (int i = 0; i < conditions.size(); i++) {
            conditions.get(i).setPlanId(planId);
            conditions.get(i).setSortOrder(i + 1);
            mapper.insert(conditions.get(i));
        }
    }
}
