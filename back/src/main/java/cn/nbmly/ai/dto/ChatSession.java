package cn.nbmly.ai.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
public class ChatSession {
    private String sessionId;
    private Long userId;
    private List<ChatMessage> messages;
    private LocalDateTime lastActiveTime;
    private int maxMessages = 50; // 最大消息数量，防止内存溢出

    public ChatSession(String sessionId, Long userId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.messages = new ArrayList<>();
        this.lastActiveTime = LocalDateTime.now();
    }

    public void addMessage(String content, String type, Object data) {
        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setType(type);
        message.setData(data);
        message.setTimestamp(LocalDateTime.now());

        messages.add(message);
        lastActiveTime = LocalDateTime.now();

        // 如果消息数量超过限制，删除最旧的消息
        if (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

    public ChatMessage getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }

    public List<ChatMessage> getRecentMessages(int count) {
        if (messages.size() <= count) {
            return new ArrayList<>(messages);
        }
        return messages.subList(messages.size() - count, messages.size());
    }

    public boolean isExpired(int timeoutMinutes) {
        return LocalDateTime.now().minusMinutes(timeoutMinutes).isAfter(lastActiveTime);
    }
}