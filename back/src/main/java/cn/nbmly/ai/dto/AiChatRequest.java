package cn.nbmly.ai.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    private String message;
    private Long userId;
}