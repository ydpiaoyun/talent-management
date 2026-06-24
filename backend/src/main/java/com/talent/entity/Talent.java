package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人才信息实体
 * <p>
 * 对应数据库 talent 表，存储人才基本信息。
 * </p>
 *
 * @author talent-hr
 */
@Data
@TableName("talent")
public class Talent {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 姓名 */
    private String name;

    /** 性别 */
    private String gender;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 部门 */
    private String dept;

    /** 职位（数据库字段名 job_title） */
    @TableField("job_title")
    private String position;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像 URL */
    private String avatarUrl;

    /** 备注 */
    private String remark;

    /** 状态：1 在职，0 离职 */
    private Integer status;

    /** 逻辑删除标记：0 未删除，1 已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
