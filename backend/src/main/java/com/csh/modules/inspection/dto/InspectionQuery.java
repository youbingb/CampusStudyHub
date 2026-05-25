package com.csh.modules.inspection.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InspectionQuery {
    private Long roomId;
    private Long inspectorId;
    private LocalDate from;
    private LocalDate to;
    private Integer page = 1;
    private Integer size = 10;
}
