package com.talent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("talent")
public class Talent {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String gender;
    private LocalDate birthDate;
    private String dept;           // 部门
    @TableField("job_title")
    private String position;       // 职位
    private String email;
    private String phone;
    private String avatarUrl;
    private String remark;
    private Integer status;        // 1在职 0离职

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
