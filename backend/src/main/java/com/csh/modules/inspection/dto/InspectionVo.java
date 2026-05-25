package com.csh.modules.inspection.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class InspectionVo {
    private Long id;
    private Long roomId;
    private String roomName;
    private Long inspectorId;
    private String inspectorName;
    private String content;
    private List<Long> issues;
    private LocalDateTime createdAt;
}
