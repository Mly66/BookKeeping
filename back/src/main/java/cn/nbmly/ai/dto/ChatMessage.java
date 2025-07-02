package cn.nbmly.ai.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private String content;
    private String type; // "user" 或 "ai"
    private Object data; // 附加数据，如账单信息
    private LocalDateTime timestamp;
}