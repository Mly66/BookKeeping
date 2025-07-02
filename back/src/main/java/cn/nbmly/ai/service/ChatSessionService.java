package cn.nbmly.ai.service;

import cn.nbmly.ai.dto.ChatSession;
import cn.nbmly.ai.dto.ChatMessage;

public interface ChatSessionService {

    /**
     * 获取或创建用户会话
     */
    ChatSession getOrCreateSession(Long userId);

    /**
     * 添加用户消息
     */
    void addUserMessage(Long userId, String content);

    /**
     * 添加AI回复消息
     */
    void addAiMessage(Long userId, String content, Object data);

    /**
     * 获取最近的对话历史
     */
    java.util.List<ChatMessage> getRecentMessages(Long userId, int count);

    /**
     * 清理过期会话
     */
    void cleanupExpiredSessions();

    /**
     * 清除用户会话
     */
    void clearSession(Long userId);
}