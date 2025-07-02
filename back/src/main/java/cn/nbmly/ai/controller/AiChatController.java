package cn.nbmly.ai.controller;

import cn.nbmly.ai.dto.AiChatRequest;
import cn.nbmly.ai.dto.AiChatResponse;
import cn.nbmly.ai.dto.ChatMessage;
import cn.nbmly.ai.service.AiChatService;
import cn.nbmly.ai.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/ai/chat")
public class AiChatController {

    private final AiChatService aiChatService;
    private final ChatSessionService chatSessionService;

    public AiChatController(AiChatService aiChatService, ChatSessionService chatSessionService) {
        this.aiChatService = aiChatService;
        this.chatSessionService = chatSessionService;
    }

    /**
     * 处理AI对话请求
     */
    @PostMapping("/process")
    public ResponseEntity<AiChatResponse> processChat(@RequestBody AiChatRequest request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body(AiChatResponse.error("请先登录"));
        }

        AiChatResponse response = aiChatService.processChat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 流式对话
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam String message,
            @RequestParam Long userId) {

        if (userId == null) {
            return Flux.just("请先登录以使用账单管理功能");
        }

        String response = aiChatService.processStreamChat(message, userId);
        return Flux.just(response)
                .doOnNext(msg -> System.out.println("AI回复: " + msg))
                .concatWith(Flux.just("[complete]"));
    }

    /**
     * 简单对话接口
     */
    @GetMapping("/simple")
    public ResponseEntity<AiChatResponse> simpleChat(
            @RequestParam String message,
            @RequestParam Long userId) {

        if (userId == null) {
            return ResponseEntity.badRequest().body(AiChatResponse.error("请先登录"));
        }

        AiChatRequest request = new AiChatRequest();
        request.setMessage(message);
        request.setUserId(userId);

        AiChatResponse response = aiChatService.processChat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int count) {

        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatMessage> history = chatSessionService.getRecentMessages(userId, count);
        return ResponseEntity.ok(history);
    }

    /**
     * 清除对话历史
     */
    @DeleteMapping("/history")
    public ResponseEntity<String> clearChatHistory(@RequestParam Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body("请先登录");
        }

        chatSessionService.clearSession(userId);
        return ResponseEntity.ok("对话历史已清除");
    }
}