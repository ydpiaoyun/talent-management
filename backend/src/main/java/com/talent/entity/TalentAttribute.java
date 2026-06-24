package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("talent_attribute")
public class TalentAttribute {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;          // 指标编码，如 height, education
    private String name;          // 指标名，如 身高、学历
    private String type;          // 数据类型: TEXT, NUMBER, ENUM, DATE, RANGE
    private String unit;          // 单位，如 cm、年
    private Double minValue;     // 范围类型最小值
    private Double maxValue;     // 范围类型最大值
    private String optionsJson;   // 枚举选项 JSON: ["博士","硕士","本科","大专"]
    private String scoreMapping;  // 分值映射 JSON: {"博士":100,"硕士":80,"本科":60,"大专":40}
    private Integer weight;       // 权重 0-100
    private Integer direction;    // 方向 1=越高越好 -1=越低越好
    private String groupName;     // 指标分组名称，如：基本信息、学历信息、工作信息
    private Integer isRequired;   // 是否必填 0否 1是
    private Integer sortOrder;    // 排序
    private Integer status;       // 状态 1启用 0禁用

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
