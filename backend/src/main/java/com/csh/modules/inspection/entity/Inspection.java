package com.csh.modules.inspection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inspection")
public class Inspection {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private Long inspectorId;
    private String content;
    private String issues;
    private LocalDateTime createdAt;
}
