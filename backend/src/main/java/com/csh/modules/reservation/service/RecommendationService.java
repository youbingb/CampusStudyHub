package com.csh.modules.reservation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csh.common.BusinessException;
import com.csh.common.constants.ReservationStatus;
import com.csh.common.constants.SeatStatus;
import com.csh.modules.reservation.dto.RecommendQuery;
import com.csh.modules.reservation.dto.RecommendVo;
import com.csh.modules.reservation.entity.Reservation;
import com.csh.modules.reservation.mapper.ReservationMapper;
import com.csh.modules.room.entity.Seat;
import com.csh.modules.room.entity.StudyRoom;
import com.csh.modules.room.mapper.SeatMapper;
import com.csh.modules.room.mapper.StudyRoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 智能推荐服务（Agent B）。
 *
 * 5 因子加权评分：
 *   room-pref      : 用户对房间的历史偏好
 *   feature-pref   : 用户对座位特性的历史偏好
 *   neighbor-free  : 相邻座位的空闲比例（独处偏好）
 *   same-seat      : 是否预约过同一座位（习惯位）
 *   conflict       : 1 - 时段冲突概率（候选其实已过滤掉硬冲突，留作可靠性奖励）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final SeatMapper seatMapper;
    private final StudyRoomMapper studyRoomMapper;
    private final ReservationMapper reservationMapper;

    @Value("${csh.recommend.weight.room-pref:0.35}")
    private double wRoomPref;
    @Value("${csh.recommend.weight.feature-pref:0.25}")
    private double wFeaturePref;
    @Value("${csh.recommend.weight.neighbor-free:0.20}")
    private double wNeighborFree;
    @Value("${csh.recommend.weight.same-seat:0.10}")
    private double wSameSeat;
    @Value("${csh.recommend.weight.conflict:0.10}")
    private double wConflict;
    @Value("${csh.recommend.default-top-n:5}")
    private int defaultTopN;

    public List<RecommendVo> recommend(Long userId, RecommendQuery q) {
        if (q.getStartTime() == null || q.getEndTime() == null) {
            throw new BusinessException("开始/结束时间必填");
        }
        if (!q.getEndTime().isAfter(q.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        int topN = q.getTopN() != null && q.getTopN() > 0 ? q.getTopN() : defaultTopN;

        // 1) 候选：所有非故障 + 房间限制（如果提供） + 房间 status=1
        LambdaQueryWrapper<Seat> seatWrap = new LambdaQueryWrapper<Seat>()
                .ne(Seat::getStatus, SeatStatus.FAULT)
                .eq(q.getRoomId() != null, Seat::getRoomId, q.getRoomId());
        List<Seat> seats = seatMapper.selectList(seatWrap);
        if (seats.isEmpty()) return List.of();

        // 限制到开放中的房间
        Set<Long> roomIds = new HashSet<>();
        for (Seat s : seats) roomIds.add(s.getRoomId());
        Map<Long, StudyRoom> roomMap = new HashMap<>();
        studyRoomMapper.selectBatchIds(roomIds).forEach(r -> {
            if (r.getStatus() != null && r.getStatus() == 1) roomMap.put(r.getId(), r);
        });
        seats = seats.stream().filter(s -> roomMap.containsKey(s.getRoomId())).toList();
        if (seats.isEmpty()) return List.of();

        // 2) 过滤掉与期望时段硬冲突的座位
        Set<Long> seatIds = new HashSet<>();
        for (Seat s : seats) seatIds.add(s.getId());
        List<Reservation> overlapping = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .in(Reservation::getSeatId, seatIds)
                .in(Reservation::getStatus, ReservationStatus.BOOKED, ReservationStatus.CHECKED_IN)
                .lt(Reservation::getStartTime, q.getEndTime())
                .gt(Reservation::getEndTime, q.getStartTime()));
        Set<Long> conflictedSeats = new HashSet<>();
        for (Reservation r : overlapping) conflictedSeats.add(r.getSeatId());
        seats = seats.stream().filter(s -> !conflictedSeats.contains(s.getId())).toList();
        if (seats.isEmpty()) return List.of();

        // 3) 用户历史
        List<Reservation> userHistory = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .in(Reservation::getStatus,
                        ReservationStatus.BOOKED, ReservationStatus.CHECKED_IN,
                        ReservationStatus.COMPLETED));

        Map<Long, Integer> roomCount = new HashMap<>();
        Map<String, Integer> featureCount = new HashMap<>();
        Set<Long> usedSeatIds = new HashSet<>();
        int totalHistory = userHistory.size();
        if (!userHistory.isEmpty()) {
            Set<Long> historySeatIds = new HashSet<>();
            for (Reservation r : userHistory) {
                roomCount.merge(r.getRoomId(), 1, Integer::sum);
                historySeatIds.add(r.getSeatId());
                usedSeatIds.add(r.getSeatId());
            }
            // 拉用户曾用过的 seat 的 feature
            if (!historySeatIds.isEmpty()) {
                seatMapper.selectBatchIds(historySeatIds).forEach(s -> {
                    for (String f : splitFeature(s.getFeature())) {
                        featureCount.merge(f, 1, Integer::sum);
                    }
                });
            }
        }

        // 4) 邻居索引：按 roomId 分组的 (row,col)->seat
        Map<Long, Map<String, Seat>> seatGridByRoom = new HashMap<>();
        for (Seat s : seats) {
            seatGridByRoom
                    .computeIfAbsent(s.getRoomId(), k -> new HashMap<>())
                    .put(s.getRowNo() + ":" + s.getColNo(), s);
        }
        // 占位/已被预约的座位也要纳入邻居判断 → 拉房间的所有 seat
        Map<Long, Map<String, Seat>> fullGridByRoom = new HashMap<>();
        List<Seat> allInRooms = seatMapper.selectList(new LambdaQueryWrapper<Seat>()
                .in(Seat::getRoomId, roomMap.keySet()));
        for (Seat s : allInRooms) {
            fullGridByRoom
                    .computeIfAbsent(s.getRoomId(), k -> new HashMap<>())
                    .put(s.getRowNo() + ":" + s.getColNo(), s);
        }
        // 在期望时段会被占用的座位 ID 集合（用于邻居空闲判断）
        Set<Long> busyInWindow = new HashSet<>(conflictedSeats);
        // 故障座位（status=FAULT 或当前 status 非 AVAILABLE）也视为非空闲邻居
        for (Seat s : allInRooms) {
            if (s.getStatus() == SeatStatus.FAULT) busyInWindow.add(s.getId());
        }

        // 5) 打分
        int maxRoomScore = roomCount.values().stream().max(Integer::compare).orElse(1);
        int maxFeatureScore = featureCount.values().stream().max(Integer::compare).orElse(1);

        List<RecommendVo> vos = new ArrayList<>(seats.size());
        for (Seat s : seats) {
            RecommendVo v = new RecommendVo();
            v.setSeatId(s.getId());
            v.setSeatNo(s.getSeatNo());
            v.setRowNo(s.getRowNo());
            v.setColNo(s.getColNo());
            v.setFeature(s.getFeature());
            v.setRoomId(s.getRoomId());
            StudyRoom room = roomMap.get(s.getRoomId());
            if (room != null) v.setRoomName(room.getName());

            List<String> reasons = new ArrayList<>();

            // room-pref
            double roomPref = 0.0;
            if (totalHistory > 0 && roomCount.containsKey(s.getRoomId())) {
                roomPref = roomCount.get(s.getRoomId()) / (double) maxRoomScore;
                if (roomPref >= 0.5) reasons.add("常去 " + (room != null ? room.getName() : "此房间"));
            }
            v.setRoomPrefScore(round(roomPref));

            // feature-pref
            double featurePref = 0.0;
            List<String> feats = splitFeature(s.getFeature());
            if (!feats.isEmpty() && !featureCount.isEmpty()) {
                int matched = 0;
                for (String f : feats) {
                    if (featureCount.containsKey(f)) matched += featureCount.get(f);
                }
                featurePref = matched / (double) (feats.size() * maxFeatureScore);
                if (featurePref >= 0.5) reasons.add("符合你常选的特性");
            }
            v.setFeaturePrefScore(round(featurePref));

            // neighbor-free
            Map<String, Seat> grid = fullGridByRoom.getOrDefault(s.getRoomId(), Map.of());
            int neighbors = 0, freeNeighbors = 0;
            int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] d : deltas) {
                Seat nb = grid.get((s.getRowNo() + d[0]) + ":" + (s.getColNo() + d[1]));
                if (nb != null) {
                    neighbors++;
                    if (!busyInWindow.contains(nb.getId())) freeNeighbors++;
                }
            }
            double neighborFree = neighbors == 0 ? 0.5 : freeNeighbors / (double) neighbors;
            v.setNeighborFreeScore(round(neighborFree));
            if (neighborFree >= 0.75) reasons.add("邻座清净");

            // same-seat
            double sameSeat = usedSeatIds.contains(s.getId()) ? 1.0 : 0.0;
            v.setSameSeatScore(round(sameSeat));
            if (sameSeat > 0) reasons.add("你坐过的位置");

            // conflict（候选已过滤硬冲突，保留作为可靠性常数）
            double conflict = 1.0;
            v.setConflictScore(round(conflict));

            double total = wRoomPref * roomPref
                    + wFeaturePref * featurePref
                    + wNeighborFree * neighborFree
                    + wSameSeat * sameSeat
                    + wConflict * conflict;
            v.setScore(round(total));
            v.setReasons(reasons);
            vos.add(v);
        }

        vos.sort(Comparator.comparingDouble(RecommendVo::getScore).reversed());
        return vos.size() > topN ? vos.subList(0, topN) : vos;
    }

    private List<String> splitFeature(String feature) {
        if (feature == null || feature.isBlank()) return List.of();
        String s = feature.trim();
        // 尝试把 JSON 数组拆掉 [、]、引号
        s = s.replaceAll("[\\[\\]\"]", "");
        List<String> out = new ArrayList<>();
        for (String tok : s.split("[,，;； ]+")) {
            if (!tok.isBlank()) out.add(tok.trim().toLowerCase());
        }
        return out;
    }

    private double round(double v) {
        return Math.round(v * 10000.0) / 10000.0;
    }
}
