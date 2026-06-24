package com.talent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.entity.SelectionCondition;
import com.talent.mapper.SelectionConditionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SelectionConditionService {

    private final SelectionConditionMapper mapper;

    public SelectionConditionService(SelectionConditionMapper mapper) {
        this.mapper = mapper;
    }

    public List<SelectionCondition> listByPlanId(Long planId) {
        return mapper.selectList(new LambdaQueryWrapper<SelectionCondition>()
                .eq(SelectionCondition::getPlanId, planId)
                .orderByAsc(SelectionCondition::getSortOrder));
    }

    @Transactional
    public void save(SelectionCondition condition) {
        if (condition.getId() == null) {
            mapper.insert(condition);
        } else {
            mapper.updateById(condition);
        }
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }

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
