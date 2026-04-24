package com.hnust.lx.interceptor;

import com.hnust.lx.context.BaseContext;
import com.hnust.lx.utils.JwtUtil;
import com.hnust.lx.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor, HandshakeInterceptor {

    private final JwtProperties jwtProperties;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getHeader("user_token");
            if (token != null && !token.isEmpty()) {
                try {
                    Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
                    Long userId = Long.valueOf(claims.get("userId").toString());
                    BaseContext.setCurrentId(userId);
                    attributes.put("userId", userId);
                    log.info("WebSocket 握手成功，用户ID: {}", userId);
                    return true;
                } catch (Exception e) {
                    log.warn("WebSocket 握手鉴权失败: {}", e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("user_token");
            if (token != null && !token.isEmpty()) {
                try {
                    Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
                    Long userId = Long.valueOf(claims.get("userId").toString());
                    BaseContext.setCurrentId(userId);
                    accessor.setUser(new StompPrincipal(userId));
                    log.info("WebSocket STOMP 鉴权成功，用户ID: {}", userId);
                } catch (Exception e) {
                    log.warn("WebSocket STOMP 鉴权失败: {}", e.getMessage());
                    throw new RuntimeException("WebSocket 鉴权失败");
                }
            }
        }
        return message;
    }

    public static class StompPrincipal implements java.security.Principal {
        private final Long userId;

        public StompPrincipal(Long userId) {
            this.userId = userId;
        }

        @Override
        public String getName() {
            return userId.toString();
        }

        public Long getUserId() {
            return userId;
        }
    }
}