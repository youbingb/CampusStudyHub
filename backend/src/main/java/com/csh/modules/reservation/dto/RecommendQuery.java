package com.csh.modules.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecommendQuery {

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /** 可选：只在某个房间内推荐 */
    private Long roomId;

    /** 返回条数，缺省走配置 csh.recommend.default-top-n */
    private Integer topN;
}
