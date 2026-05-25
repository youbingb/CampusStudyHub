package com.csh.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csh.common.constants.NotificationType;
import com.csh.common.constants.ReservationStatus;
import com.csh.modules.notification.service.NotificationService;
import com.csh.modules.report.service.CreditService;
import com.csh.modules.reservation.entity.Reservation;
import com.csh.modules.reservation.mapper.ReservationMapper;
import com.csh.modules.room.service.SeatStatusService;
import com.csh.modules.system.dto.RuleVo;
import com.csh.modules.system.service.RuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约定时任务（Agent B owner）：
 * 1) 未签到超时（startTime + grace_min < now 仍是 BOOKED）→ EXPIRED + 扣信誉 + 通知 + 刷座位
 * 2) 已签到到期（endTime < now 仍是 CHECKED_IN）→ COMPLETED + checkOutTime=endTime + 刷座位
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationMapper reservationMapper;
    private final SeatStatusService seatStatusService;
    private final NotificationService notificationService;
    private final CreditService creditService;
    private final RuleService ruleService;

    /** 每 30 秒扫一次：在线测试下方便观察，正式部署可调大 */
    @Scheduled(fixedDelay = 30_000, initialDelay = 15_000)
    public void tick() {
        try {
            handleExpired();
            handleAutoComplete();
        } catch (Exception e) {
            log.warn("[reservation-scheduler] tick error: {}", e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleExpired() {
        RuleVo rule = ruleService.current();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusMinutes(rule.getCheckInGraceMin());
        // 找出 startTime <= cutoff 仍 BOOKED 的预约
        List<Reservation> list = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, ReservationStatus.BOOKED)
                .le(Reservation::getStartTime, cutoff));
        int penalty = rule.getNoShowCreditPenalty() == null ? 0 : rule.getNoShowCreditPenalty();
        for (Reservation r : list) {
            r.setStatus(ReservationStatus.EXPIRED);
            reservationMapper.updateById(r);
            try { seatStatusService.refresh(r.getSeatId()); } catch (Exception e) { log.warn("refresh failed: {}", e.getMessage()); }
            if (penalty > 0) {
                try {
                    creditService.changeCredit(r.getUserId(), -penalty, "预约未签到",
                            "RESERVATION", r.getId());
                } catch (Exception e) {
                    log.warn("[reservation-scheduler] credit deduct failed reservation={} err={}",
                            r.getId(), e.getMessage());
                }
            }
            try {
                notificationService.send(r.getUserId(), NotificationType.RESERVATION_EXPIRED,
                        "预约已超时",
                        "您未在签到宽限期内签到，预约 #" + r.getId() + " 已自动取消"
                                + (penalty > 0 ? "，并扣除信誉 " + penalty + " 分" : ""),
                        r.getId());
            } catch (Exception e) {
                log.warn("[reservation-scheduler] notify expired failed: {}", e.getMessage());
            }
        }
        if (!list.isEmpty()) {
            log.info("[reservation-scheduler] expired {} bookings", list.size());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleAutoComplete() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> list = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, ReservationStatus.CHECKED_IN)
                .lt(Reservation::getEndTime, now));
        for (Reservation r : list) {
            r.setStatus(ReservationStatus.COMPLETED);
            if (r.getCheckOutTime() == null) {
                r.setCheckOutTime(r.getEndTime());
            }
            reservationMapper.updateById(r);
            try { seatStatusService.refresh(r.getSeatId()); } catch (Exception e) { log.warn("refresh failed: {}", e.getMessage()); }
        }
        if (!list.isEmpty()) {
            log.info("[reservation-scheduler] auto-completed {} checked-in reservations", list.size());
        }
    }
}
