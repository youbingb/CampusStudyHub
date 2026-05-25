package com.csh.modules.reservation.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendVo {
    private Long seatId;
    private String seatNo;
    private Integer rowNo;
    private Integer colNo;
    private String feature;
    private Long roomId;
    private String roomName;

    /** 总分（0~1） */
    private Double score;
    /** 5 因子分量（便于前端展示） */
    private Double roomPrefScore;
    private Double featurePrefScore;
    private Double neighborFreeScore;
    private Double sameSeatScore;
    private Double conflictScore;
    /** 命中的推荐理由 */
    private List<String> reasons;
}
