package cn.nbmly.ai.controller;

import cn.nbmly.ai.services.BillTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@RestController
@CrossOrigin
@RequestMapping("/api/ai")
public class OpenAiController {

        private final ChatClient chatClient;
        private final BillTools billTools;

        public OpenAiController(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, BillTools billTools) {
                this.billTools = billTools;
                this.chatClient = chatClientBuilder
                                .defaultSystem("""
                                                您是一个智能记账助手，可以帮助用户管理个人账单。
                                                您可以执行以下操作：
                                                1. 创建账单：记录收入或支出
                                                2. 查询账单：查看特定条件的账单
                                                3. 更新账单：修改已有账单信息
                                                4. 删除账单：删除不需要的账单
                                                5. 查看统计：显示账单统计信息
                                                6. 查看分类：显示用户的账单分类

                                                请根据用户的需求调用相应的函数来完成任务。
                                                在创建或更新账单时，请从用户描述中提取金额、类型、分类、备注和时间信息。
                                                在查询账单时，请根据用户的关键词进行智能匹配。
                                                请用中文回复，保持友好和专业的语气。
                                                今天的日期是 {current_date}.
                                                """)
                                .defaultAdvisors(
                                                new PromptChatMemoryAdvisor(chatMemory))
                                .build();
        }

        @GetMapping(value = "/generateStreamAsString", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<String> generateStreamAsString(
                        @RequestParam(value = "message", defaultValue = "你好") String message,
                        @RequestParam(value = "userId", required = false) Long userId) {
                System.out.println("收到消息: " + message + ", 用户ID: " + userId);

                if (userId == null) {
                        return Flux.just("请先登录以使用账单管理功能");
                }

                return chatClient.prompt()
                                .system(s -> s.param("current_date", LocalDate.now().toString()))
                                .advisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                                .user(message)
                                .stream()
                                .content()
                                .doOnNext(response -> System.out.println("发送响应: " + response))
                                .doOnError(error -> System.err.println("发生错误: " + error.getMessage()))
                                .concatWith(Flux.just("[complete]"));
        }

        // 账单管理函数 - 创建账单
        public Object createBill(String description, Long userId) {
                BillTools.setCurrentUserId(userId);
                return billTools.createBill(description);
        }

        // 账单管理函数 - 查询账单
        public Object queryBills(String query, Long userId) {
                BillTools.setCurrentUserId(userId);
                return billTools.queryBills(query);
        }

        // 账单管理函数 - 更新账单
        public Object updateBill(Long billId, String description, Long userId) {
                BillTools.setCurrentUserId(userId);
                return billTools.updateBill(billId, description);
        }

        // 账单管理函数 - 删除账单
        public Object deleteBill(Long billId, Long userId) {
                BillTools.setCurrentUserId(userId);
                return billTools.deleteBill(billId);
        }

        // 账单管理函数 - 获取用户分类
        public Object getUserCategories(Long userId) {
                BillTools.setCurrentUserId(userId);
                return billTools.getUserCategories();
        }

        // 账单管理函数 - 获取账单统计
        public Object getBillStatistics(Long userId) {
                BillTools.setCurrentUserId(userId);
                return billTools.getBillStatistics();
        }
}
