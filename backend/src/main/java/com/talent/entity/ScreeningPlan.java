package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 筛选方案实体
 * <p>
 * 对应数据库 screening_plan 表，定义人才筛选方案。
 * </p>
 *
 * @author talent-hr
 */
@Data
@TableName("screening_plan")
public class ScreeningPlan {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 方案名称 */
    private String name;

    /** 方案描述 */
    private String description;

    /** 条件间逻辑类型：AND（且）、OR（或） */
    private String logicType;

    /** 高级筛选表达式（Aviator 语法，非必填） */
    private String expression;

    /** 状态：1 启用，0 禁用 */
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
