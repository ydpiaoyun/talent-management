package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("selection_condition")
public class SelectionCondition {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;           // 所属筛选方案ID
    private String name;           // 条件名，如"学历要求"
    private Long attrId;           // 指标ID
    @TableField("`operator`")
    private String operator;       // 操作符: EQ, NE, GT, GTE, LT, LTE, IN, LIKE, NOT_LIKE
    @TableField("`value`")
    private String value;          // 比较值
    private Integer sortOrder;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
