package com.csh.modules.room.dto;

import com.csh.common.constants.SeatStatus;
import lombok.Data;

@Data
public class SeatVo {
    private Long id;
    private Long roomId;
    private String seatNo;
    private Integer rowNo;
    private Integer colNo;
    private SeatStatus status;
    private String feature;
}
