package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 人才指标值实体
 * <p>
 * 对应数据库 talent_attr_value 表，存储人才各项指标的具体值。
 * </p>
 *
 * @author talent-hr
 */
@Data
@TableName("talent_attr_value")
public class TalentAttrValue {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 人才 ID */
    private Long talentId;

    /** 指标 ID */
    private Long attrId;

    /** 文本值 */
    private String valueText;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
