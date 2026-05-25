package com.csh.modules.notification.dto;

import com.csh.common.constants.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatPushPayload {
    private Long seatId;
    private SeatStatus status;
    private LocalDateTime updatedAt;
}
