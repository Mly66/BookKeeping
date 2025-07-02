package cn.nbmly.ai.service.impl;

import cn.nbmly.ai.dto.ChatSession;
import cn.nbmly.ai.dto.ChatMessage;
import cn.nbmly.ai.service.ChatSessionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    private final Map<Long, ChatSession> sessions = new ConcurrentHashMap<>();
    private static final int SESSION_TIMEOUT_MINUTES = 30; // 30分钟超时

    @Override
    public ChatSession getOrCreateSession(Long userId) {
        ChatSession session = sessions.get(userId);
        if (session == null || session.isExpired(SESSION_TIMEOUT_MINUTES)) {
            session = new ChatSession("session_" + userId, userId);
            sessions.put(userId, session);
        }
        return session;
    }

    @Override
    public void addUserMessage(Long userId, String content) {
        ChatSession session = getOrCreateSession(userId);
        session.addMessage(content, "user", null);
    }

    @Override
    public void addAiMessage(Long userId, String content, Object data) {
        ChatSession session = getOrCreateSession(userId);
        session.addMessage(content, "ai", data);
    }

    @Override
    public List<ChatMessage> getRecentMessages(Long userId, int count) {
        ChatSession session = sessions.get(userId);
        if (session == null) {
            return List.of();
        }
        return session.getRecentMessages(count);
    }

    @Override
    @Scheduled(fixedRate = 300000) // 每5分钟清理一次
    public void cleanupExpiredSessions() {
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired(SESSION_TIMEOUT_MINUTES));
    }

    @Override
    public void clearSession(Long userId) {
        sessions.remove(userId);
    }
}