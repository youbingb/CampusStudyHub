package com.csh.modules.report.dto;

import com.csh.common.constants.ReportStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportVo {
    private Long id;
    private String type;
    private String description;
    private String evidenceUrl;
    private ReportStatus status;
    private String result;

    private Long reporterId;
    private String reporterName;

    private Long targetUserId;
    private String targetUserName;

    private Long reservationId;
    private Long seatId;

    private Long handlerId;
    private String handlerName;
    private LocalDateTime handledAt;

    private LocalDateTime createdAt;
}
