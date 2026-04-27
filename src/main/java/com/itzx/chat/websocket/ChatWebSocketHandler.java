package com.itzx.chat.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itzx.chat.entity.ChatMessage;
import com.itzx.chat.mapper.ChatMessageMapper;
import com.itzx.order.entity.Orders;
import com.itzx.order.mapper.OrdersMapper;
import com.itzx.until.JwtUtils;
import com.itzx.user.entity.User;
import com.itzx.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // orderNo -> sessions
    private final Map<String, Set<WebSocketSession>> orderSessions = new ConcurrentHashMap<>();
    // sessionId -> orderNo / role / username
    private final Map<String, String> sessionOrderNo = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRole = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUsername = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, String> params = parseQueryParams(session.getUri());
        String token = params.get("token");
        String orderNo = params.get("orderNo");
        if (token == null || orderNo == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("缺少token或orderNo"));
            return;
        }
        // token 直接为 JWT 字符串
        if (!jwtUtils.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token无效或已过期"));
            return;
        }
        String username = jwtUtils.getUsernameFromToken(token);
        String role = jwtUtils.getRoleFromToken(token);
        if (username == null || role == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("身份信息无效"));
            return;
        }
        Orders order = ordersMapper.findByOrderNo(orderNo);
        if (order == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("订单不存在"));
            return;
        }
        // 普通买家只能进入自己的订单会话
        if (!"ADMIN".equals(role) && !"MERCHANT".equals(role)) {
            User user = userMapper.login(username);
            if (user == null || order.getUserId() == null || !order.getUserId().equals(user.getId())) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("无权限加入该订单会话"));
                return;
            }
        }

        sessionOrderNo.put(session.getId(), orderNo);
        sessionRole.put(session.getId(), role);
        sessionUsername.put(session.getId(), username);
        orderSessions
                .computeIfAbsent(orderNo, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String orderNo = sessionOrderNo.get(session.getId());
        String role = sessionRole.get(session.getId());
        String username = sessionUsername.get(session.getId());
        if (orderNo == null || role == null || username == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("会话未初始化"));
            return;
        }
        String payload = message.getPayload();
        JsonNode node;
        try {
            node = objectMapper.readTree(payload);
        } catch (IOException e) {
            return;
        }
        String content = node.has("content") ? node.get("content").asText() : null;
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // 保存到数据库
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setOrderNo(orderNo);
        chatMessage.setFromRole(role);
        chatMessage.setFromUsername(username);
        chatMessage.setContent(content);
        chatMessageMapper.insert(chatMessage);

        // 构造下发给前端的消息
        Map<String, Object> out = new HashMap<>();
        out.put("type","CHAT");
        out.put("orderNo","orderNo");
        out.put("fromRole",role);
        out.put("fromUsername",username);
        out.put("content",content);
        out.put("timestamp", LocalDateTime.now().toString());
        String outJson = objectMapper.writeValueAsString(out);
        TextMessage outMsg = new TextMessage(outJson);

        Set<WebSocketSession> sessions = orderSessions.get(orderNo);
        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(outMsg);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String orderNo = sessionOrderNo.remove(session.getId());
        sessionRole.remove(session.getId());
        sessionUsername.remove(session.getId());
        if (orderNo != null) {
            Set<WebSocketSession> sessions = orderSessions.get(orderNo);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    orderSessions.remove(orderNo);
                }
            }
        }
    }

    private Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> map = new HashMap<>();
        if (uri == null || uri.getQuery() == null) {
            return map;
        }
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }
}
