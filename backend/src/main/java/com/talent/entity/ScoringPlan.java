package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评分方案实体
 * <p>
 * 对应数据库 scoring_plan 表，定义人才评分方案。
 * </p>
 *
 * @author talent-hr
 */
@Data
@TableName("scoring_plan")
public class ScoringPlan {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评分方案名称 */
    private String name;

    /** 备注 */
    private String remark;

    /** Aviator 评分表达式，变量名为指标 code */
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
