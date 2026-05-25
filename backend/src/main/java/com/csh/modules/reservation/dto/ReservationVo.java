package com.csh.modules.reservation.dto;

import com.csh.common.constants.ReservationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationVo {
    private Long id;
    private Long userId;
    private String username;
    private String userRealName;
    private Long seatId;
    private String seatNo;
    private Long roomId;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDateTime createdAt;
}
