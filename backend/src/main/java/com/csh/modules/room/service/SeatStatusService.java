package com.csh.modules.room.service;

import com.csh.common.constants.SeatStatus;

/**
 * 座位状态跨模块契约（见 docs/AGENTS.md §3.3，Agent B owner）。
 * 其他模块（reservation/report/inspection）通过此接口修改座位状态，并由实现负责广播。
 */
public interface SeatStatusService {

    /** 根据 reservation 时段表重新计算座位状态，写回 seat.status，并广播。 */
    void refresh(Long seatId);

    /** 故障标记 + 广播 + 写 seat_fault。 */
    void markFault(Long seatId, Long reporterId, String reason);

    /** 解除故障：标记 seat_fault FIXED，并按当前预约状态重算 seat.status。 */
    void clearFault(Long seatId);
}
