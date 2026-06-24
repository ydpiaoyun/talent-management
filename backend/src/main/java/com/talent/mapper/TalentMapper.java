package com.talent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.talent.entity.Talent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人才信息 Mapper 接口
 *
 * @author talent-hr
 */
@Mapper
public interface TalentMapper extends BaseMapper<Talent> {
}
