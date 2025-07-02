package cn.nbmly.ai.config;

import cn.nbmly.ai.services.BillTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory, BillTools billTools) {
        log.info("=== 创建ChatClient Bean ===");
        log.info("OpenAiChatModel: {}", openAiChatModel);
        log.info("ChatMemory: {}", chatMemory);
        log.info("BillTools: {}", billTools);
        System.out.println("=== 创建ChatClient Bean ===");
        System.out.println("OpenAiChatModel: " + openAiChatModel);
        System.out.println("ChatMemory: " + chatMemory);
        System.out.println("BillTools: " + billTools);
        
        // 获取当前东八区时间
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        String currentTime = now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"));
        
        String systemPrompt = String.format("""
            你是一个智能记账助手，可以帮助用户管理账单。请根据用户的需求调用相应的工具函数。
            
            当前时间信息：
            - 系统时间：%s (东八区)
            - 时区：Asia/Shanghai (UTC+8)
            
            重要提示：
            1. 当用户询问时间相关问题时，请使用上述系统时间信息
            2. 当用户说"今天"、"昨天"、"前天"时，请基于当前日期 %s 进行计算
            3. 账单时间戳会自动使用系统当前时间，无需手动指定
            4. 请根据用户的需求智能调用相应的工具函数
            """, currentTime, now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .build();
        
        log.info("ChatClient创建成功: {}", chatClient);
        log.info("系统提示词: {}", systemPrompt);
        System.out.println("ChatClient创建成功: " + chatClient);
        System.out.println("系统提示词: " + systemPrompt);
        
        return chatClient;
    }
} 