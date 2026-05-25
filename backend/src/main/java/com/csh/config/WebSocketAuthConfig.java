package com.csh.config;

import com.csh.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

/**
 * 在 STOMP CONNECT 帧解析 Authorization: Bearer <jwt>，把 userId 设为 STOMP 会话的 Principal.name，
 * 让 SimpMessagingTemplate.convertAndSendToUser(userId, "/queue/notifications", ...) 能路由到正确会话。
 *
 * <p>不修改既有 WebSocketConfig（主题前缀已锁），通过另一个 Configurer Bean 注入入站拦截器。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
                if (StompCommand.CONNECT.equals(acc.getCommand())) {
                    String auth = acc.getFirstNativeHeader("Authorization");
                    if (auth != null && auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
                        String token = auth.substring(7).trim();
                        try {
                            Claims claims = jwtUtil.parse(token);
                            String userId = claims.getSubject();
                            acc.setUser(new StompPrincipal(userId));
                        } catch (Exception e) {
                            log.debug("STOMP CONNECT JWT parse failed: {}", e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }

    private record StompPrincipal(String name) implements Principal {
        @Override public String getName() { return name; }
    }
}
