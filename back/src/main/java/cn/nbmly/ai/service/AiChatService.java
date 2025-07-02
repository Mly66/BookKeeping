package cn.nbmly.ai.service;

import cn.nbmly.ai.dto.AiChatRequest;
import cn.nbmly.ai.dto.AiChatResponse;

public interface AiChatService {

    /**
     * 处理AI对话
     */
    AiChatResponse processChat(AiChatRequest request);

    /**
     * 处理流式对话
     */
    String processStreamChat(String message, Long userId);
}