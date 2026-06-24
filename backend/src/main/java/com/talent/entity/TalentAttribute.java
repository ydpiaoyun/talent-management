package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 人才评价指标实体
 * <p>
 * 对应数据库 talent_attribute 表，定义可配置的评价指标。
 * </p>
 *
 * @author talent-hr
 */
@Data
@TableName("talent_attribute")
public class TalentAttribute {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 指标编码，如 height、education */
    private String code;

    /** 指标名称，如 身高、学历 */
    private String name;

    /** 数据类型：TEXT、NUMBER、ENUM、DATE、RANGE */
    private String type;

    /** 单位，如 cm、年 */
    private String unit;

    /** 范围类型最小值 */
    private Double minValue;

    /** 范围类型最大值 */
    private Double maxValue;

    /** 枚举选项 JSON，如 ["博士","硕士","本科","大专"] */
    private String optionsJson;

    /** 分值映射 JSON，如 {"博士":100,"硕士":80,"本科":60,"大专":40} */
    private String scoreMapping;

    /** 权重 0-100 */
    private Integer weight;

    /** 方向：1 越高越好，-1 越低越好 */
    private Integer direction;

    /** 指标分组名称，如：基本信息、学历信息、工作信息 */
    private String groupName;

    /** 是否必填：0 否，1 是 */
    private Integer isRequired;

    /** 排序序号 */
    private Integer sortOrder;

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
