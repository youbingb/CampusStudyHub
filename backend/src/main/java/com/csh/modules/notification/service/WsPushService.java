package com.csh.modules.notification.service;

import com.csh.common.constants.SeatStatus;
import com.csh.modules.notification.dto.SeatPushPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 跨模块 WebSocket 推送服务（Agent A owner）。
 * 契约见 docs/AGENTS.md §3.2，签名不允许更改。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WsPushService {

    private final SimpMessagingTemplate messagingTemplate;

    /** 座位状态广播：/topic/rooms/{roomId}/seats */
    public void publishSeat(Long roomId, Long seatId, SeatStatus status) {
        if (roomId == null || seatId == null) return;
        SeatPushPayload payload = new SeatPushPayload(seatId, status, LocalDateTime.now());
        String dest = "/topic/rooms/" + roomId + "/seats";
        try {
            messagingTemplate.convertAndSend(dest, payload);
        } catch (Exception e) {
            log.warn("publishSeat failed dest={} err={}", dest, e.getMessage());
        }
    }

    /** 个人通道推送：/user/{username}/queue/notifications */
    public void publishToUser(Long userId, Object payload) {
        if (userId == null) return;
        try {
            // 用 userId 作为 principal name；前端订阅 /user/queue/notifications 时 STOMP 会自动按 principal 路由
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", payload);
        } catch (Exception e) {
            log.warn("publishToUser failed userId={} err={}", userId, e.getMessage());
        }
    }
}
