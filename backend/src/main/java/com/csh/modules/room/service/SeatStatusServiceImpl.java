package com.csh.modules.room.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csh.common.BusinessException;
import com.csh.common.constants.ReservationStatus;
import com.csh.common.constants.SeatStatus;
import com.csh.modules.notification.service.WsPushService;
import com.csh.modules.reservation.entity.Reservation;
import com.csh.modules.reservation.mapper.ReservationMapper;
import com.csh.modules.room.entity.Seat;
import com.csh.modules.room.entity.SeatFault;
import com.csh.modules.room.mapper.SeatFaultMapper;
import com.csh.modules.room.mapper.SeatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatStatusServiceImpl implements SeatStatusService {

    private final SeatMapper seatMapper;
    private final SeatFaultMapper seatFaultMapper;
    private final ReservationMapper reservationMapper;
    private final WsPushService wsPushService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refresh(Long seatId) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }
        SeatStatus oldStatus = seat.getStatus();

        // 1. 存在 OPEN 状态的故障 → FAULT
        Long openFaults = seatFaultMapper.selectCount(new LambdaQueryWrapper<SeatFault>()
                .eq(SeatFault::getSeatId, seatId)
                .eq(SeatFault::getStatus, "OPEN"));
        SeatStatus computed;
        if (openFaults != null && openFaults > 0) {
            computed = SeatStatus.FAULT;
        } else {
            // 2. 当前时间是否在某条 CHECKED_IN 时段内 → OCCUPIED
            // 3. 当前时间是否在某条 BOOKED 时段内 → RESERVED
            // 4. 都不是 → AVAILABLE
            // 注意：CHECKED_IN 不要求 startTime<=now，因为用户可能在宽限期内提前签到
            LocalDateTime now = LocalDateTime.now();
            List<Reservation> active = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                    .eq(Reservation::getSeatId, seatId)
                    .in(Reservation::getStatus, ReservationStatus.BOOKED, ReservationStatus.CHECKED_IN)
                    .and(w -> w
                            .and(inner -> inner
                                    .eq(Reservation::getStatus, ReservationStatus.CHECKED_IN)
                                    .ge(Reservation::getEndTime, now))
                            .or(inner -> inner
                                    .eq(Reservation::getStatus, ReservationStatus.BOOKED)
                                    .le(Reservation::getStartTime, now)
                                    .ge(Reservation::getEndTime, now))));
            if (active.stream().anyMatch(r -> r.getStatus() == ReservationStatus.CHECKED_IN)) {
                computed = SeatStatus.OCCUPIED;
            } else if (!active.isEmpty()) {
                computed = SeatStatus.RESERVED;
            } else {
                computed = SeatStatus.AVAILABLE;
            }
        }

        if (computed != oldStatus) {
            seat.setStatus(computed);
            seatMapper.updateById(seat);
        }
        wsPushService.publishSeat(seat.getRoomId(), seatId, computed);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFault(Long seatId, Long reporterId, String reason) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }
        SeatFault fault = new SeatFault();
        fault.setSeatId(seatId);
        fault.setReporterId(reporterId);
        fault.setDescription(reason);
        fault.setStatus("OPEN");
        seatFaultMapper.insert(fault);

        seat.setStatus(SeatStatus.FAULT);
        seatMapper.updateById(seat);
        wsPushService.publishSeat(seat.getRoomId(), seatId, SeatStatus.FAULT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearFault(Long seatId) {
        List<SeatFault> opens = seatFaultMapper.selectList(new LambdaQueryWrapper<SeatFault>()
                .eq(SeatFault::getSeatId, seatId)
                .eq(SeatFault::getStatus, "OPEN"));
        LocalDateTime now = LocalDateTime.now();
        for (SeatFault f : opens) {
            f.setStatus("FIXED");
            f.setFixedAt(now);
            seatFaultMapper.updateById(f);
        }
        refresh(seatId);
    }
}
