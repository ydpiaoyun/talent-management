package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 筛选条件实体
 * <p>
 * 对应数据库 selection_condition 表，定义筛选方案中的单个条件。
 * </p>
 *
 * @author talent-hr
 */
@Data
@TableName("selection_condition")
public class SelectionCondition {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属筛选方案 ID */
    private Long planId;

    /** 条件名称，如"学历要求" */
    private String name;

    /** 指标 ID */
    private Long attrId;

    /** 操作符：EQ、NE、GT、GTE、LT、LTE、IN、LIKE、NOT_LIKE */
    @TableField("`operator`")
    private String operator;

    /** 比较值 */
    @TableField("`value`")
    private String value;

    /** 排序序号 */
    private Integer sortOrder;

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
