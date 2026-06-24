package com.talent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.talent.entity.TalentScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TalentScoreMapper extends BaseMapper<TalentScore> {

    @Select("SELECT s.talent_id, s.total_score, s.detail_json, t.name, t.dept " +
            "FROM talent_score s JOIN talent t ON s.talent_id = t.id " +
            "WHERE s.plan_id = #{planId} ORDER BY s.total_score DESC")
    List<Map<String, Object>> rankingByPlan(@Param("planId") Long planId);
}
