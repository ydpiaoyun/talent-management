package com.talent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.talent.entity.TalentAttrValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TalentAttrValueMapper extends BaseMapper<TalentAttrValue> {

    @Select("SELECT v.talent_id  AS TALENT_ID, v.attr_id AS ATTR_ID, v.value_text AS VALUE_TEXT " +
            "FROM talent_attr_value v " +
            "LEFT JOIN talent_attribute a ON v.attr_id = a.id " +
            "WHERE v.talent_id IN (${talentIds}) " +
            "ORDER BY v.talent_id, a.sort_order")
    List<Map<String, Object>> batchFindByTalentIds(@Param("talentIds") String talentIds);

    @Select("SELECT v.value_text FROM talent_attr_value v " +
            "WHERE v.talent_id = #{talentId} AND v.attr_id = #{attrId} LIMIT 1")
    String findValueByTalentAndAttr(@Param("talentId") Long talentId, @Param("attrId") Long attrId);
}
