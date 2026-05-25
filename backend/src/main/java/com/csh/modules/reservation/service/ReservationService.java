package com.csh.modules.reservation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csh.common.BusinessException;
import com.csh.common.PageResult;
import com.csh.common.constants.NotificationType;
import com.csh.common.constants.ReservationStatus;
import com.csh.common.constants.SeatStatus;
import com.csh.modules.notification.service.NotificationService;
import com.csh.modules.report.service.CreditService;
import com.csh.modules.reservation.dto.CreateReservationReq;
import com.csh.modules.reservation.dto.ReservationQuery;
import com.csh.modules.reservation.dto.ReservationVo;
import com.csh.modules.reservation.entity.Reservation;
import com.csh.modules.reservation.mapper.ReservationMapper;
import com.csh.modules.room.entity.Seat;
import com.csh.modules.room.entity.StudyRoom;
import com.csh.modules.room.mapper.SeatMapper;
import com.csh.modules.room.mapper.StudyRoomMapper;
import com.csh.modules.room.service.SeatStatusService;
import com.csh.modules.system.dto.RuleVo;
import com.csh.modules.system.service.RuleService;
import com.csh.modules.user.entity.SysUser;
import com.csh.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationMapper reservationMapper;
    private final SeatMapper seatMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final SysUserMapper sysUserMapper;
    private final SeatStatusService seatStatusService;
    private final NotificationService notificationService;
    private final CreditService creditService;
    private final RuleService ruleService;

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, CreateReservationReq req) {
        LocalDateTime now = LocalDateTime.now();
        if (req.getStartTime().isBefore(now.minusMinutes(1))) {
            throw new BusinessException(4000, "开始时间不能早于当前时间");
        }
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new BusinessException(4000, "结束时间必须晚于开始时间");
        }
        Seat seat = seatMapper.selectById(req.getSeatId());
        if (seat == null) {
            throw new BusinessException(4000, "座位不存在");
        }
        if (seat.getStatus() == SeatStatus.FAULT) {
            throw new BusinessException(4000, "座位处于故障状态，无法预约");
        }

        RuleVo rule = ruleService.current();

        // 信誉门槛
        int score = creditService.getScore(userId);
        if (score < rule.getMinCredit()) {
            throw new BusinessException(4002, "信誉分不足，无法预约（当前 " + score + "，下限 " + rule.getMinCredit() + "）");
        }

        // 时长限制
        long durationMinutes = Duration.between(req.getStartTime(), req.getEndTime()).toMinutes();
        if (durationMinutes > rule.getMaxDurationHours() * 60L) {
            throw new BusinessException(4004, "单次预约时长不能超过 " + rule.getMaxDurationHours() + " 小时");
        }

        // 提前天数限制
        LocalDate startDate = req.getStartTime().toLocalDate();
        LocalDate today = LocalDate.now();
        long daysAhead = startDate.toEpochDay() - today.toEpochDay();
        if (daysAhead > rule.getMaxAdvanceDays()) {
            throw new BusinessException(4004, "最多可提前 " + rule.getMaxAdvanceDays() + " 天预约");
        }

        // 每日上限
        LocalDateTime dayStart = startDate.atStartOfDay();
        LocalDateTime dayEnd = startDate.atTime(LocalTime.MAX);
        Long dailyCount = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .in(Reservation::getStatus,
                        ReservationStatus.BOOKED, ReservationStatus.CHECKED_IN, ReservationStatus.COMPLETED)
                .ge(Reservation::getStartTime, dayStart)
                .le(Reservation::getStartTime, dayEnd));
        if (dailyCount != null && dailyCount >= rule.getMaxDaily()) {
            throw new BusinessException(4003, "今日预约数已达上限 " + rule.getMaxDaily());
        }

        // 时段冲突：同座位、状态为 BOOKED|CHECKED_IN，且时段相交（s < newEnd && e > newStart）
        Long conflict = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getSeatId, req.getSeatId())
                .in(Reservation::getStatus, ReservationStatus.BOOKED, ReservationStatus.CHECKED_IN)
                .lt(Reservation::getStartTime, req.getEndTime())
                .gt(Reservation::getEndTime, req.getStartTime()));
        if (conflict != null && conflict > 0) {
            throw new BusinessException(4001, "该时段已被预约");
        }

        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setSeatId(seat.getId());
        r.setRoomId(seat.getRoomId());
        r.setStartTime(req.getStartTime());
        r.setEndTime(req.getEndTime());
        r.setStatus(ReservationStatus.BOOKED);
        reservationMapper.insert(r);

        // 状态广播 + 通知
        try { seatStatusService.refresh(seat.getId()); } catch (Exception e) { log.warn("refresh seat failed: {}", e.getMessage()); }
        StudyRoom room = studyRoomMapper.selectById(seat.getRoomId());
        String roomName = room != null ? room.getName() : ("房间" + seat.getRoomId());
        notificationService.send(userId, NotificationType.RESERVATION_CREATED,
                "预约成功",
                "已预约 " + roomName + " · " + seat.getSeatNo() + "，时段 " + req.getStartTime() + " 至 " + req.getEndTime(),
                r.getId());
        return r.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long reservationId) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null) throw new BusinessException("预约不存在");
        if (!r.getUserId().equals(userId)) throw new BusinessException(403, "只能取消自己的预约");
        if (r.getStatus() != ReservationStatus.BOOKED) {
            throw new BusinessException("只能取消未签到的预约");
        }
        r.setStatus(ReservationStatus.CANCELLED);
        reservationMapper.updateById(r);
        try { seatStatusService.refresh(r.getSeatId()); } catch (Exception e) { log.warn("refresh failed: {}", e.getMessage()); }
        notificationService.send(userId, NotificationType.RESERVATION_CANCELLED,
                "预约已取消", "您主动取消了预约 #" + reservationId, reservationId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminCancel(Long reservationId, String reason) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null) throw new BusinessException("预约不存在");
        if (r.getStatus() == ReservationStatus.CANCELLED
                || r.getStatus() == ReservationStatus.COMPLETED
                || r.getStatus() == ReservationStatus.EXPIRED) {
            throw new BusinessException("预约已经结束，无需取消");
        }
        r.setStatus(ReservationStatus.CANCELLED);
        reservationMapper.updateById(r);
        try { seatStatusService.refresh(r.getSeatId()); } catch (Exception e) { log.warn("refresh failed: {}", e.getMessage()); }
        notificationService.send(r.getUserId(), NotificationType.RESERVATION_CANCELLED,
                "预约被管理员取消", "您的预约 #" + reservationId + " 已被取消，原因：" + reason, reservationId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long userId, Long reservationId) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null) throw new BusinessException("预约不存在");
        if (!r.getUserId().equals(userId)) throw new BusinessException(403, "只能为自己的预约签到");
        if (r.getStatus() != ReservationStatus.BOOKED) {
            throw new BusinessException("当前状态无法签到");
        }
        LocalDateTime now = LocalDateTime.now();
        RuleVo rule = ruleService.current();
        LocalDateTime earliest = r.getStartTime().minusMinutes(rule.getCheckInGraceMin());
        if (now.isBefore(earliest)) {
            throw new BusinessException("距离签到时间还早，最早可在 " + earliest + " 签到");
        }
        if (now.isAfter(r.getEndTime())) {
            throw new BusinessException("预约时段已结束");
        }
        r.setStatus(ReservationStatus.CHECKED_IN);
        r.setCheckInTime(now);
        reservationMapper.updateById(r);
        try { seatStatusService.refresh(r.getSeatId()); } catch (Exception e) { log.warn("refresh failed: {}", e.getMessage()); }
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long userId, Long reservationId) {
        Reservation r = reservationMapper.selectById(reservationId);
        if (r == null) throw new BusinessException("预约不存在");
        if (!r.getUserId().equals(userId)) throw new BusinessException(403, "只能为自己的预约签退");
        if (r.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BusinessException("当前状态无法签退");
        }
        r.setStatus(ReservationStatus.COMPLETED);
        r.setCheckOutTime(LocalDateTime.now());
        reservationMapper.updateById(r);
        try { seatStatusService.refresh(r.getSeatId()); } catch (Exception e) { log.warn("refresh failed: {}", e.getMessage()); }
    }

    public ReservationVo detail(Long id) {
        Reservation r = reservationMapper.selectById(id);
        if (r == null) throw new BusinessException("预约不存在");
        return enrich(r);
    }

    public PageResult<ReservationVo> page(ReservationQuery q) {
        Page<Reservation> page = new Page<>(q.getPage() == null ? 1 : q.getPage(),
                q.getSize() == null ? 10 : q.getSize());
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<Reservation>()
                .eq(q.getStatus() != null, Reservation::getStatus, q.getStatus())
                .eq(q.getUserId() != null, Reservation::getUserId, q.getUserId())
                .eq(q.getSeatId() != null, Reservation::getSeatId, q.getSeatId())
                .eq(q.getRoomId() != null, Reservation::getRoomId, q.getRoomId())
                .ge(q.getStartFrom() != null, Reservation::getStartTime, q.getStartFrom())
                .le(q.getStartTo() != null, Reservation::getStartTime, q.getStartTo())
                .orderByDesc(Reservation::getStartTime);
        IPage<Reservation> result = reservationMapper.selectPage(page, wrapper);
        List<ReservationVo> vos = enrichBatch(result.getRecords());
        PageResult<ReservationVo> pr = new PageResult<>();
        pr.setRecords(vos);
        pr.setTotal(result.getTotal());
        pr.setPages(result.getPages());
        pr.setCurrent(result.getCurrent());
        pr.setSize(result.getSize());
        return pr;
    }

    private ReservationVo enrich(Reservation r) {
        return enrichBatch(List.of(r)).get(0);
    }

    private List<ReservationVo> enrichBatch(List<Reservation> list) {
        if (list == null || list.isEmpty()) return List.of();
        Set<Long> userIds = new HashSet<>();
        Set<Long> seatIds = new HashSet<>();
        Set<Long> roomIds = new HashSet<>();
        for (Reservation r : list) {
            userIds.add(r.getUserId());
            seatIds.add(r.getSeatId());
            roomIds.add(r.getRoomId());
        }
        Map<Long, SysUser> users = new HashMap<>();
        if (!userIds.isEmpty()) {
            sysUserMapper.selectBatchIds(userIds).forEach(u -> users.put(u.getId(), u));
        }
        Map<Long, Seat> seats = new HashMap<>();
        if (!seatIds.isEmpty()) {
            seatMapper.selectBatchIds(seatIds).forEach(s -> seats.put(s.getId(), s));
        }
        Map<Long, StudyRoom> rooms = new HashMap<>();
        if (!roomIds.isEmpty()) {
            studyRoomMapper.selectBatchIds(roomIds).forEach(rm -> rooms.put(rm.getId(), rm));
        }
        return list.stream().map(r -> {
            ReservationVo vo = new ReservationVo();
            vo.setId(r.getId());
            vo.setUserId(r.getUserId());
            SysUser u = users.get(r.getUserId());
            if (u != null) { vo.setUsername(u.getUsername()); vo.setUserRealName(u.getRealName()); }
            vo.setSeatId(r.getSeatId());
            Seat s = seats.get(r.getSeatId());
            if (s != null) vo.setSeatNo(s.getSeatNo());
            vo.setRoomId(r.getRoomId());
            StudyRoom rm = rooms.get(r.getRoomId());
            if (rm != null) vo.setRoomName(rm.getName());
            vo.setStartTime(r.getStartTime());
            vo.setEndTime(r.getEndTime());
            vo.setStatus(r.getStatus());
            vo.setCheckInTime(r.getCheckInTime());
            vo.setCheckOutTime(r.getCheckOutTime());
            vo.setCreatedAt(r.getCreatedAt());
            return vo;
        }).toList();
    }
}
