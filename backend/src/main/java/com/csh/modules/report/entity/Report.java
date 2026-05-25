package com.csh.modules.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.csh.common.constants.ReportStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private Long targetUserId;
    private Long reservationId;
    private Long seatId;
    private String type;
    private String description;
    private String evidenceUrl;
    private ReportStatus status;
    private String result;
    private Long handlerId;
    private LocalDateTime handledAt;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
