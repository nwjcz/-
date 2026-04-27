package com.itzx.chat.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itzx.chat.entity.UserChatMessage;
import com.itzx.chat.entity.UserChatSession;
import com.itzx.chat.mapper.UserChatMessageMapper;
import com.itzx.chat.mapper.UserChatSessionMapper;
import com.itzx.until.JwtUtils;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserChatMessageMapper userChatMessageMapper;

    @Autowired
    private UserChatSessionMapper userChatSessionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // buyerUsername -> sessions（买家自己的会话 + 商家针对该买家的单会话）
    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    // sessionId -> buyerUsername / role / username
    private final Map<String, String> sessionBuyerUsername = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRole = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUsername = new ConcurrentHashMap<>();

    // 商家/管理员的全局会话通道（不绑定具体 buyerUsername）
    private final Set<WebSocketSession> merchantGlobalSessions = ConcurrentHashMap.newKeySet();
    private final Map<String, Boolean> sessionIsMerchantGlobal = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, String> params = parseQueryParams(session.getUri());
        String token = params.get("token");
        String buyerUsernameParam = params.get("buyerUsername");
        if (token == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("缺少token"));
            return;
        }
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

        // 普通买家：会话绑定自己的用户名
        if (!"ADMIN".equals(role) && !"MERCHANT".equals(role)) {
            String buyerUsername = username;
            sessionBuyerUsername.put(session.getId(), buyerUsername);
            sessionRole.put(session.getId(), role);
            sessionUsername.put(session.getId(), username);
            userSessions.computeIfAbsent(buyerUsername, k -> ConcurrentHashMap.newKeySet())
                    .add(session);
            return;
        }

        // 商家/管理员：如果带了 buyerUsername，则是针对单个买家的会话；
        // 如果没带，则作为全局客服通道接收所有买家的新消息。
        if (buyerUsernameParam == null || buyerUsernameParam.isEmpty()) {
            merchantGlobalSessions.add(session);
            sessionIsMerchantGlobal.put(session.getId(), true);
            sessionRole.put(session.getId(), role);
            sessionUsername.put(session.getId(), username);
            return;
        }

        String buyerUsername = buyerUsernameParam;
        sessionBuyerUsername.put(session.getId(), buyerUsername);
        sessionRole.put(session.getId(), role);
        sessionUsername.put(session.getId(), username);
        userSessions.computeIfAbsent(buyerUsername, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String buyerUsername = sessionBuyerUsername.get(session.getId());
        String role = sessionRole.get(session.getId());
        String username = sessionUsername.get(session.getId());
        Boolean isGlobal = sessionIsMerchantGlobal.get(session.getId());

        // 全局商家通道不直接发送消息，只负责接收和展示
        if (Boolean.TRUE.equals(isGlobal)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("全局会话仅用于接收消息"));
            return;
        }

        if (buyerUsername == null || role == null || username == null) {
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
        UserChatMessage chatMessage = new UserChatMessage();
        chatMessage.setBuyerUsername(buyerUsername);
        chatMessage.setFromRole(role);
        chatMessage.setFromUsername(username);
        chatMessage.setContent(content);
        userChatMessageMapper.insert(chatMessage);

        // 更新/插入会话记录（用于会话列表和未读/未回复状态）
        UserChatSession sessionRecord = userChatSessionMapper.findByBuyerUsername(buyerUsername);
        String now = java.time.LocalDateTime.now().toString();
        if (sessionRecord == null) {
            sessionRecord = new UserChatSession();
            sessionRecord.setBuyerUsername(buyerUsername);
            sessionRecord.setBuyerDisplayName(buyerUsername);
            sessionRecord.setLastMsgContent(content);
            sessionRecord.setLastMsgTime(now);
            sessionRecord.setLastMsgFrom(role);
            // 买家发消息：未读+1，标记未回复
            if ("USER".equals(role)) {
                sessionRecord.setUnreadCount(1);
                sessionRecord.setHasUnreplied(true);
            } else {
                sessionRecord.setUnreadCount(0);
                sessionRecord.setHasUnreplied(false);
            }
            sessionRecord.setCreatedAt(now);
            sessionRecord.setUpdatedAt(now);
            userChatSessionMapper.insert(sessionRecord);
        } else {
            if ("USER".equals(role)) {
                userChatSessionMapper.updateOnBuyerMessage(buyerUsername, content, now);
            } else {
                userChatSessionMapper.updateOnMerchantMessage(buyerUsername, content, now);
            }
        }

        // 构造下发给前端的消息
        Map<String, Object> out = new HashMap<>();
        out.put("type", "CHAT");
        out.put("buyerUsername", buyerUsername);
        out.put("fromRole", role);
        out.put("fromUsername", username);
        out.put("content", content);
        out.put("timestamp", LocalDateTime.now().toString());
        String outJson = objectMapper.writeValueAsString(out);
        TextMessage outMsg = new TextMessage(outJson);

        Set<WebSocketSession> sessions = userSessions.get(buyerUsername);
        if (sessions != null) {
            for (WebSocketSession s : new HashSet<>(sessions)) {
                if (s.isOpen()) {
                    s.sendMessage(outMsg);
                }
            }
        }

        // 推送给所有商家/管理员全局会话
        for (WebSocketSession s : new HashSet<>(merchantGlobalSessions)) {
            if (s.isOpen()) {
                s.sendMessage(outMsg);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Boolean isGlobal = sessionIsMerchantGlobal.remove(session.getId());
        if (Boolean.TRUE.equals(isGlobal)) {
            merchantGlobalSessions.remove(session);
        } else {
            String buyerUsername = sessionBuyerUsername.remove(session.getId());
            if (buyerUsername != null) {
                Set<WebSocketSession> sessions = userSessions.get(buyerUsername);
                if (sessions != null) {
                    sessions.remove(session);
                    if (sessions.isEmpty()) {
                        userSessions.remove(buyerUsername);
                    }
                }
            }
        }
        sessionRole.remove(session.getId());
        sessionUsername.remove(session.getId());
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
