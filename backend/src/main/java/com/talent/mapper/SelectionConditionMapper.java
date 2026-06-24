package com.talent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.talent.entity.SelectionCondition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 筛选条件 Mapper 接口
 *
 * @author talent-hr
 */
@Mapper
public interface SelectionConditionMapper extends BaseMapper<SelectionCondition> {
}
