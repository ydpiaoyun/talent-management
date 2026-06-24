package com.talent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.talent.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper 接口
 *
 * @author talent-hr
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
