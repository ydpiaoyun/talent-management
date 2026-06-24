package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("screening_plan")
public class ScreeningPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;           // 方案名
    private String description;    // 方案描述
    private String logicType;      // 条件间逻辑: AND / OR
    private String expression;     // 高级筛选表达式（Aviator语法，非必填）
    private Integer status;        // 1启用 0禁用

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
