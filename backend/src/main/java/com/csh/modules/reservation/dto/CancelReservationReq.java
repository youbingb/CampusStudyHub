package com.csh.modules.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelReservationReq {

    @NotBlank(message = "取消原因不能为空")
    private String reason;
}
