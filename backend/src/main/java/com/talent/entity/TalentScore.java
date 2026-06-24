package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("talent_score")
public class TalentScore {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long talentId;
    private Long planId;
    private Double totalScore;
    private String detailJson;     // {"education":80,"work_years":90,...}

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime calcTime;
}
