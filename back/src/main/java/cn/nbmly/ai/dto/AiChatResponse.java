package cn.nbmly.ai.dto;

import lombok.Data;

@Data
public class AiChatResponse {
    private String message;
    private String status;
    private Object data;

    public static AiChatResponse success(String message) {
        AiChatResponse response = new AiChatResponse();
        response.setMessage(message);
        response.setStatus("success");
        return response;
    }

    public static AiChatResponse success(String message, Object data) {
        AiChatResponse response = new AiChatResponse();
        response.setMessage(message);
        response.setStatus("success");
        response.setData(data);
        return response;
    }

    public static AiChatResponse error(String message) {
        AiChatResponse response = new AiChatResponse();
        response.setMessage(message);
        response.setStatus("error");
        return response;
    }
}