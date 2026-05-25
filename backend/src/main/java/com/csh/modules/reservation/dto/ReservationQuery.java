package com.csh.modules.reservation.dto;

import com.csh.common.constants.ReservationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationQuery {
    private Integer page = 1;
    private Integer size = 10;
    private ReservationStatus status;
    private Long userId;
    private Long seatId;
    private Long roomId;
    private LocalDateTime startFrom;
    private LocalDateTime startTo;
}
