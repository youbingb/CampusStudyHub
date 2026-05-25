package com.csh.modules.room.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seat_fault")
public class SeatFault {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long seatId;
    private Long reporterId;
    private String description;
    private String status;
    private LocalDateTime fixedAt;
    private LocalDateTime createdAt;
}
