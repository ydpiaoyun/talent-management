package com.talent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.talent.entity.TalentScore;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人才评分记录 Mapper 接口
 * <p>
 * 仅继承 BaseMapper 的基础 CRUD 方法，复杂查询在 Service 层用 MyBatis-Plus 代码方式实现。
 * </p>
 *
 * @author talent-hr
 */
@Mapper
public interface TalentScoreMapper extends BaseMapper<TalentScore> {
}
