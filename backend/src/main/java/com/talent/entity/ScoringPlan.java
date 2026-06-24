package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("scoring_plan")
public class ScoringPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;           // 评分方案名
    private String remark;
    private String expression;      // Aviator评分表达式，变量名为指标code
    private Integer status;        // 1启用 0禁用

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
