package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("talent_attr_value")
public class TalentAttrValue {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long talentId;         // 人才ID
    private Long attrId;           // 指标ID
    private String valueText;      // 文本值

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
