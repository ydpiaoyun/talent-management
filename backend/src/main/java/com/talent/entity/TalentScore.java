package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 人才评分记录实体
 * <p>
 * 对应数据库 talent_score 表，存储评分方案计算后的人才得分。
 * </p>
 *
 * @author talent-hr
 */
@Data
@TableName("talent_score")
public class TalentScore {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 人才 ID */
    private Long talentId;

    /** 评分方案 ID */
    private Long planId;

    /** 总得分 */
    private Double totalScore;

    /** 各指标得分明细 JSON，如 {"education":80,"work_years":90} */
    private String detailJson;

    /** 计算时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime calcTime;
}
