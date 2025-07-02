package cn.nbmly.ai.service.impl;

import cn.nbmly.ai.dto.AiChatRequest;
import cn.nbmly.ai.dto.AiChatResponse;
import cn.nbmly.ai.dto.BillDTO;
import cn.nbmly.ai.dto.CategoryDTO;
import cn.nbmly.ai.dto.ChatMessage;
import cn.nbmly.ai.dto.CategoryRequest;
import cn.nbmly.ai.dto.UserDTO;
import cn.nbmly.ai.entity.User;
import cn.nbmly.ai.service.AiBillService;
import cn.nbmly.ai.service.AiChatService;
import cn.nbmly.ai.service.ChatSessionService;
import cn.nbmly.ai.service.CategoryService;
import cn.nbmly.ai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import cn.nbmly.ai.services.BillTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiBillService aiBillService;
    private final ChatSessionService chatSessionService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final BillTools billTools;
    @Autowired(required = false)
    private ChatClient chatClient;
    
    // 请求频率控制
    private static final Map<Long, Long> lastRequestTime = new ConcurrentHashMap<>();
    private static final long MIN_REQUEST_INTERVAL = 1000; // 最小请求间隔1秒

    @Override
    public AiChatResponse processChat(AiChatRequest request) {
        String message = request.getMessage().toLowerCase();
        Long userId = request.getUserId();

        try {
            log.info("=== 开始处理AI聊天请求 ===");
            log.info("用户消息: {}", request.getMessage());
            log.info("用户ID: {}", userId);
            System.out.println("=== 开始处理AI聊天请求 ===");
            System.out.println("用户消息: " + request.getMessage());
            System.out.println("用户ID: " + userId);

            // 获取用户信息
            UserDTO userInfo = getUserInfo(userId);
            String userNickname = userInfo.getNickname() != null ? userInfo.getNickname() : userInfo.getUsername();
            log.info("用户昵称: {}", userNickname);

            // 记录用户消息
            chatSessionService.addUserMessage(userId, request.getMessage());

            // 处理上下文引用
            String processedMessage = processContextReferences(message, userId);
            log.info("处理后的消息: {}", processedMessage);

            /*
            // 帮助信息 - 优先级最高
            if (isHelp(processedMessage)) {
                String response = getHelpMessage();
                chatSessionService.addAiMessage(userId, response, null);
                return AiChatResponse.success(response);
            }

            // 查看统计 - 优先级较高
            if (isViewStatistics(processedMessage)) {
                String statistics = aiBillService.getBillStatistics(userId);
                chatSessionService.addAiMessage(userId, statistics, null);
                return AiChatResponse.success(statistics);
            }

            // 创建分类 - 在查看分类之前处理
            if (isCreateCategory(processedMessage)) {
                try {
                    // 解析分类名称和类型
                    String type = processedMessage.contains("收入") ? "income"
                            : (processedMessage.contains("支出") ? "expense" : "expense");

                    String name = processedMessage;
                    String[] removeWords = { "添加", "创建", "增加", "一个", "类别", "分类", "收入", "支出", "的", "请", "帮我" };
                    for (String w : removeWords) {
                        name = name.replace(w, "");
                    }
                    name = name.replaceAll("\\s+", "").trim();

                    if (name.isEmpty()) {
                        String errorMsg = "❌ 无法解析分类名称，请重试，例如：添加一个奖金的收入分类";
                        chatSessionService.addAiMessage(userId, errorMsg, null);
                        return AiChatResponse.error(errorMsg);
                    }

                    CategoryRequest categoryRequest = new CategoryRequest();
                    categoryRequest.setName(name);
                    categoryRequest.setType(type);

                    // 创建分类
                    cn.nbmly.ai.dto.CategoryDTO createdCategory = categoryService.createCategory(categoryRequest,
                            userId);

                    String typeText = "income".equals(type) ? "收入" : "支出";
                    String response = String.format("✅ 已创建分类 '%s'（%s）", createdCategory.getName(), typeText);
                    chatSessionService.addAiMessage(userId, response, createdCategory);
                    return AiChatResponse.success(response, createdCategory);
                } catch (Exception e) {
                    String errorMsg = "❌ 创建分类失败：" + e.getMessage();
                    chatSessionService.addAiMessage(userId, errorMsg, null);
                    return AiChatResponse.error(errorMsg);
                }
            }

            // 查看分类
            if (isViewCategories(processedMessage)) {
                List<CategoryDTO> categories = aiBillService.getUserCategories(userId);
                String response = "您的账单分类：";
                chatSessionService.addAiMessage(userId, response, categories);
                return AiChatResponse.success(response, categories);
            }

            // 删除分类 - 在删除账单逻辑之前处理
            if (isDeleteCategory(processedMessage)) {
                String categoryName = extractCategoryName(processedMessage);
                if (categoryName == null) {
                    String errorMsg = "❌ 请指定要删除的分类名称";
                    chatSessionService.addAiMessage(userId, errorMsg, null);
                    return AiChatResponse.error(errorMsg);
                }

                try {
                    // 先删除该分类下的所有账单
                    List<BillDTO> deletedBills = aiBillService.deleteBillsByCategory(categoryName, userId);

                    // 再删除分类本身
                    List<CategoryDTO> categories = categoryService.getCategoriesByUser(userId);
                    CategoryDTO targetCat = categories.stream()
                            .filter(cat -> categoryName.equals(cat.getName()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("未找到分类:" + categoryName));

                    categoryService.deleteCategory(targetCat.getId(), userId);

                    // 获取最新分类列表
                    List<CategoryDTO> updatedCategories = categoryService.getCategoriesByUser(userId);

                    String response = String.format("✅ 已删除分类 '%s' 以及相关账单 %d 条", categoryName, deletedBills.size());
                    chatSessionService.addAiMessage(userId, response, updatedCategories);
                    return AiChatResponse.success(response, updatedCategories);
                } catch (Exception e) {
                    String errorMsg = "❌ 删除分类失败：" + e.getMessage();
                    chatSessionService.addAiMessage(userId, errorMsg, null);
                    return AiChatResponse.error(errorMsg);
                }
            }

            // 删除账单或分类
            if (isDeleteBill(processedMessage)) {
                // 先检查是否是删除分类
                String categoryName = extractCategoryName(processedMessage);
                if (categoryName != null) {
                    try {
                        List<BillDTO> deletedBills = aiBillService.deleteBillsByCategory(categoryName, userId);
                        String response = String.format("✅ 已删除分类'%s'的所有账单，共%d条", categoryName, deletedBills.size());
                        chatSessionService.addAiMessage(userId, response, deletedBills);
                        return AiChatResponse.success(response, deletedBills);
                    } catch (Exception e) {
                        String errorMsg = "❌ 删除分类失败：" + e.getMessage();
                        chatSessionService.addAiMessage(userId, errorMsg, null);
                        return AiChatResponse.error(errorMsg);
                    }
                }

                // 再检查是否是删除单个账单
                Long billId = extractBillId(processedMessage, userId);
                if (billId != null) {
                    try {
                        aiBillService.deleteBill(billId, userId);
                        String response = "✅ 账单删除成功！";
                        chatSessionService.addAiMessage(userId, response, null);
                        return AiChatResponse.success(response);
                    } catch (Exception e) {
                        String errorMsg = "❌ 删除账单失败：" + e.getMessage();
                        chatSessionService.addAiMessage(userId, errorMsg, null);
                        return AiChatResponse.error(errorMsg);
                    }
                }

                String errorMsg = "❌ 请指定要删除的账单ID或分类名称";
                chatSessionService.addAiMessage(userId, errorMsg, null);
                return AiChatResponse.error(errorMsg);
            }

            // 查询账单
            if (isQueryBill(processedMessage)) {
                List<BillDTO> bills = aiBillService.queryBills(request.getMessage(), userId);
                if (bills.isEmpty()) {
                    String response = "📭 没有找到符合条件的账单";
                    chatSessionService.addAiMessage(userId, response, null);
                    return AiChatResponse.success(response);
                }

                // 如果是统计查询，提供汇总信息
                if (isStatisticsQuery(processedMessage)) {
                    String summary = generateStatisticsSummary(bills, processedMessage);
                    chatSessionService.addAiMessage(userId, summary, bills);
                    return AiChatResponse.success(summary, bills);
                }

                String response = "📋 查询结果：";
                chatSessionService.addAiMessage(userId, response, bills);
                return AiChatResponse.success(response, bills);
            }

            // 更新账单
            if (isUpdateBill(processedMessage)) {
                Long billId = extractBillId(processedMessage, userId);
                if (billId == null) {
                    String errorMsg = "❌ 请指定要更新的账单ID";
                    chatSessionService.addAiMessage(userId, errorMsg, null);
                    return AiChatResponse.error(errorMsg);
                }
                try {
                    BillDTO bill = aiBillService.updateBill(billId, request.getMessage(), userId);
                    String response = "✅ 账单更新成功！";
                    chatSessionService.addAiMessage(userId, response, bill);
                    return AiChatResponse.success(response, bill);
                } catch (Exception e) {
                    String errorMsg = "❌ 更新账单失败：" + e.getMessage();
                    chatSessionService.addAiMessage(userId, errorMsg, null);
                    return AiChatResponse.error(errorMsg);
                }
            }

            // 创建账单
            if (isCreateBill(processedMessage)) {
                try {
                    BillDTO bill = aiBillService.createBill(request.getMessage(), userId);
                    String response = generateCreateBillResponse(bill);
                    chatSessionService.addAiMessage(userId, response, bill);
                    return AiChatResponse.success(response, bill);
                } catch (Exception e) {
                    String errorMsg = "❌ 创建账单失败：" + e.getMessage();
                    chatSessionService.addAiMessage(userId, errorMsg, null);
                    return AiChatResponse.error(errorMsg);
                }
            }
            */

            // ==========规则分流全部未命中时，交给LLM+@AiFunction自动处理==========
            log.info("=== 开始AI工具调用 ===");
            log.info("ChatClient: {}", chatClient);
            log.info("BillTools: {}", billTools);
            System.out.println("=== 开始AI工具调用 ===");
            System.out.println("ChatClient: " + chatClient);
            System.out.println("BillTools: " + billTools);
            
            // 检查请求频率
            long currentTime = System.currentTimeMillis();
            Long lastTime = lastRequestTime.get(userId);
            if (lastTime != null && currentTime - lastTime < MIN_REQUEST_INTERVAL) {
                String waitMsg = "⏰ 请求过于频繁，请稍等1秒后重试。";
                chatSessionService.addAiMessage(userId, waitMsg, null);
                return AiChatResponse.error(waitMsg);
            }
            lastRequestTime.put(userId, currentTime);
            
            if (chatClient != null && billTools != null) {
                log.info("调用ChatClient处理用户消息");
                System.out.println("调用ChatClient处理用户消息");
                
                // 设置当前用户ID
                BillTools.setCurrentUserId(userId);
                
                // 构建包含用户信息的消息
                String userMessage = String.format("用户昵称：%s\n用户消息：%s", userNickname, request.getMessage());
                
                String aiReply = chatClient.prompt()
                        .user(userMessage)
                        .tools(billTools)
                        .call()
                        .content();
                
                log.info("AI回复: {}", aiReply);
                System.out.println("AI回复: " + aiReply);
                
                chatSessionService.addAiMessage(userId, aiReply, null);
                return AiChatResponse.success(aiReply);
            } else {
                log.warn("ChatClient或BillTools为空，无法进行AI工具调用");
                System.out.println("ChatClient或BillTools为空，无法进行AI工具调用");
            }

            // 默认回复
            String response = "🤖 我是您的智能记账助手！\n\n" + getHelpMessage();
            chatSessionService.addAiMessage(userId, response, null);
            return AiChatResponse.success(response);

        } catch (Exception e) {
            log.error("处理AI聊天请求失败", e);
            System.out.println("处理AI聊天请求失败: " + e.getMessage());
            
            // 特殊处理API速率限制错误
            if (e.getMessage() != null && e.getMessage().contains("rate_limit_reached_error")) {
                String errorMsg = "🤖 AI助手暂时繁忙，请稍等1-2秒后重试。\n\n💡 提示：这是API调用频率限制，属于正常现象。";
                chatSessionService.addAiMessage(userId, errorMsg, null);
                return AiChatResponse.error(errorMsg);
            }
            
            String errorMsg = "❌ 操作失败：" + e.getMessage();
            chatSessionService.addAiMessage(userId, errorMsg, null);
            return AiChatResponse.error(errorMsg);
        }
    }

    /**
     * 获取用户信息
     */
    private UserDTO getUserInfo(Long userId) {
        try {
            // 通过UserService根据用户ID获取用户信息
            User user = userService.findById(userId);
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            userDTO.setEmail(user.getEmail());
            userDTO.setPhone(user.getPhone());
            userDTO.setAvatar(user.getAvatar());
            userDTO.setCreateTime(user.getCreateTime());
            return userDTO;
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            // 如果获取失败，返回默认用户信息
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userId);
            userDTO.setUsername("用户" + userId);
            userDTO.setNickname("用户" + userId);
            return userDTO;
        }
    }

    @Override
    public String processStreamChat(String message, Long userId) {
        AiChatRequest request = new AiChatRequest();
        request.setMessage(message);
        request.setUserId(userId);

        AiChatResponse response = processChat(request);

        // 如果有数据，格式化显示
        if (response.getData() != null) {
            if (response.getData() instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) response.getData();
                if (list.isEmpty()) {
                    return response.getMessage();
                }

                StringBuilder result = new StringBuilder(response.getMessage() + "\n\n");

                // 检查是否是账单数据
                if (!list.isEmpty() && list.get(0) instanceof BillDTO) {
                    for (int i = 0; i < list.size(); i++) {
                        BillDTO bill = (BillDTO) list.get(i);
                        String typeText = "expense".equals(bill.getType()) ? "支出" : "收入";
                        String amountText = "expense".equals(bill.getType()) ? "-" : "+";
                        result.append(String.format("%d. 账单#%d | %s | %s￥%.2f | %s\n",
                                i + 1, bill.getId(), typeText, amountText, bill.getAmount(),
                                bill.getCategoryName() != null ? bill.getCategoryName() : "未知分类"));
                    }
                }
                // 检查是否是分类数据
                else if (!list.isEmpty() && list.get(0) instanceof CategoryDTO) {
                    for (int i = 0; i < list.size(); i++) {
                        CategoryDTO category = (CategoryDTO) list.get(i);
                        String typeText = "expense".equals(category.getType()) ? "支出" : "收入";
                        result.append(String.format("%d. %s (%s)\n",
                                i + 1, category.getName(), typeText));
                    }
                }
                // 其他类型数据
                else {
                    for (int i = 0; i < list.size(); i++) {
                        result.append(i + 1).append(". ").append(list.get(i).toString()).append("\n");
                    }
                }
                return result.toString();
            } else {
                return response.getMessage() + "\n\n详细信息：\n" + response.getData().toString();
            }
        }

        return response.getMessage();
    }

    private boolean isCreateBill(String message) {
        // 检查是否包含金额信息（数字+元/块等）
        Pattern amountPattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(元|块|块钱|RMB|¥)");
        boolean hasAmount = amountPattern.matcher(message).find();

        // 检查是否是查询语句（包含"多少"、"查询"、"查看"等）
        boolean isQuery = message.contains("多少") || message.contains("查询") ||
                message.contains("查看") || message.contains("显示") ||
                message.contains("统计") || message.contains("汇总") ||
                message.contains("花了多少钱") || message.contains("收入多少") || message.contains("支出多少");

        // 检查是否包含创建账单的关键词
        boolean hasCreateKeywords = message.contains("记录") || message.contains("添加") || message.contains("创建") ||
                message.contains("记") || message.contains("花") || message.contains("赚") ||
                message.contains("收入") || message.contains("支出") || message.contains("消费") ||
                message.contains("买") || message.contains("支付") || message.contains("转账");

        // 只有包含金额、不是查询语句、且包含创建关键词才认为是创建账单
        return hasAmount && !isQuery && hasCreateKeywords;
    }

    private boolean isQueryBill(String message) {
        // 如果包含更新关键词，避免与查询混淆
        if (message.contains("修改") || message.contains("更新") || message.contains("编辑") || message.contains("改")) {
            return false;
        }

        // 基本查询关键词
        boolean hasQueryKeywords = message.contains("查询") || message.contains("查看") || message.contains("显示") ||
                message.contains("账单") || message.contains("记录") || message.contains("所有")|| message.contains("给出");

        // 金额查询关键词
        boolean hasAmountQuery = message.contains("花了多少钱") || message.contains("收入多少") || message.contains("支出多少") ||
                message.contains("多少");

        // 如果包含具体分类或时间信息，优先认为是账单查询
        boolean hasSpecificInfo = message.contains("吃饭") || message.contains("餐饮") ||
                message.contains("交通") || message.contains("购物") || message.contains("娱乐") ||
                message.contains("医疗") || message.contains("教育") || message.contains("住房") ||
                message.contains("今天") || message.contains("昨天") || message.contains("这个月") ||
                message.contains("分类");

        return hasQueryKeywords || (hasAmountQuery && hasSpecificInfo) || hasAmountQuery;
    }

    private boolean isUpdateBill(String message) {
        return message.contains("修改") || message.contains("更新") || message.contains("编辑") ||
                message.contains("改");
    }

    private boolean isDeleteBill(String message) {
        return message.contains("删除") || message.contains("移除") || message.contains("删");
    }

    private boolean isViewCategories(String message) {
        return (message.contains("分类") || message.contains("类别")) &&
                !message.contains("统计") && !message.contains("汇总");
    }

    private boolean isViewStatistics(String message) {
        // 总体统计查询的关键词
        boolean isGeneralStats = message.contains("账单统计") || message.contains("查看统计") ||
                message.contains("总体统计") || message.contains("汇总统计") ||
                (message.contains("收入") && message.contains("支出") && !message.contains("分类")) ||
                message.contains("余额") || message.contains("总结");

        // 如果包含具体分类关键词，则不认为是总体统计
        boolean hasSpecificCategory = message.contains("吃饭") || message.contains("餐饮") ||
                message.contains("交通") || message.contains("购物") || message.contains("娱乐") ||
                message.contains("医疗") || message.contains("教育") || message.contains("住房") ||
                message.contains("今天") || message.contains("昨天") || message.contains("这个月") ||
                message.contains("分类");

        // 如果包含时间关键词且包含分类关键词，则不认为是总体统计
        boolean hasTimeAndCategory = (message.contains("今天") || message.contains("昨天") ||
                message.contains("这个月") || message.contains("上个月")) &&
                (message.contains("花了多少钱") || message.contains("收入多少") || message.contains("支出多少"));

        return isGeneralStats && !hasSpecificCategory && !hasTimeAndCategory;
    }

    private boolean isHelp(String message) {
        return message.contains("帮助") || message.contains("help") || message.contains("功能") ||
                message.contains("能做什么");
    }

    private Long extractBillId(String message, Long userId) {
        // 匹配"删除账单123"格式
        Pattern pattern1 = Pattern.compile("删除账单(\\d+)");
        Matcher matcher1 = pattern1.matcher(message);
        if (matcher1.find()) {
            return Long.parseLong(matcher1.group(1));
        }

        // 匹配"删除123号账单"格式
        Pattern pattern2 = Pattern.compile("删除(\\d+)号账单");
        Matcher matcher2 = pattern2.matcher(message);
        if (matcher2.find()) {
            return Long.parseLong(matcher2.group(1));
        }

        // 匹配"账单123"格式
        Pattern pattern3 = Pattern.compile("账单(\\d+)");
        Matcher matcher3 = pattern3.matcher(message);
        if (matcher3.find()) {
            return Long.parseLong(matcher3.group(1));
        }

        // 匹配"123号"格式
        Pattern pattern4 = Pattern.compile("(\\d+)号");
        Matcher matcher4 = pattern4.matcher(message);
        if (matcher4.find()) {
            return Long.parseLong(matcher4.group(1));
        }

        // 处理上下文引用，如"删除刚才那个账单"
        if (message.contains("刚才") || message.contains("那个") || message.contains("这个")) {
            return extractBillIdFromContext(userId);
        }

        // 最后尝试匹配任何数字
        Pattern pattern5 = Pattern.compile("(\\d+)");
        Matcher matcher5 = pattern5.matcher(message);
        if (matcher5.find()) {
            return Long.parseLong(matcher5.group(1));
        }

        return null;
    }

    /**
     * 处理上下文引用
     */
    private String processContextReferences(String message, Long userId) {
        String processedMessage = message;

        // 处理"刚才那个账单"等上下文引用
        if (message.contains("刚才") || message.contains("那个") || message.contains("这个") ||
                message.contains("上面") || message.contains("下面") || message.contains("之前")) {
            Long billId = extractBillIdFromContext(userId);
            if (billId != null) {
                processedMessage = message.replaceAll("(刚才|那个|这个|上面|下面|之前)", billId.toString());
            }
        }

        // 处理"删除第一个"、"删除第二个"等序号引用
        Pattern orderPattern = Pattern.compile("删除第(\\d+)个");
        Matcher orderMatcher = orderPattern.matcher(message);
        if (orderMatcher.find()) {
            int order = Integer.parseInt(orderMatcher.group(1));
            Long billId = extractBillIdByOrder(userId, order);
            if (billId != null) {
                processedMessage = message.replaceAll("删除第\\d+个", "删除账单" + billId);
            }
        }

        return processedMessage;
    }

    /**
     * 从上下文中提取账单ID
     */
    private Long extractBillIdFromContext(Long userId) {
        List<ChatMessage> recentMessages = chatSessionService.getRecentMessages(userId, 10);

        // 查找最近的账单查询结果
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            ChatMessage msg = recentMessages.get(i);
            if ("ai".equals(msg.getType()) && msg.getData() instanceof List) {
                List<?> data = (List<?>) msg.getData();
                if (!data.isEmpty() && data.get(0) instanceof BillDTO) {
                    // 返回第一个账单的ID
                    return ((BillDTO) data.get(0)).getId();
                }
            }
        }

        return null;
    }

    /**
     * 按序号提取账单ID
     */
    private Long extractBillIdByOrder(Long userId, int order) {
        List<ChatMessage> recentMessages = chatSessionService.getRecentMessages(userId, 10);

        // 查找最近的账单查询结果
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            ChatMessage msg = recentMessages.get(i);
            if ("ai".equals(msg.getType()) && msg.getData() instanceof List) {
                List<?> data = (List<?>) msg.getData();
                if (!data.isEmpty() && data.get(0) instanceof BillDTO) {
                    // 检查序号是否有效
                    if (order > 0 && order <= data.size()) {
                        return ((BillDTO) data.get(order - 1)).getId();
                    }
                }
            }
        }

        return null;
    }

    private String getHelpMessage() {
        return """
                📋 我可以帮您完成以下操作：

                💰 创建账单：
                • "今天花了100元买午餐"
                • "收入5000元工资"
                • "昨天消费200元打车"

                🔍 查询账单：
                • "查看所有账单"
                • "查询餐饮支出"
                • "查看今天的账单"

                ✏️ 更新账单：
                • "修改账单123为200元"
                • "更新账单456的备注"

                🗑️ 删除账单：
                • "删除账单123"
                • "删除123号账单"
                • "删除分类吃屎"
                • "把分类吃屎的账单删除"

                📊 查看统计：
                • "查看账单统计"
                • "我今天吃饭花了多少钱"
                • "查看收入统计"

                🏷️ 查看分类：
                • "查看我的分类"
                • "显示所有分类"

                🧠 上下文记忆功能：
                • 我会记住我们的对话历史
                • 支持"删除刚才那个账单"等上下文引用
                • 会话保持30分钟，自动清理过期会话

                💡 获取帮助：
                • "帮助" 或 "help"

                🎯 小贴士：您可以用自然语言与我对话，我会智能识别您的意图并记住上下文！
                """;
    }

    private boolean isStatisticsQuery(String message) {
        return message.contains("统计") || message.contains("汇总") || message.contains("总结") ||
                message.contains("余额") || message.contains("收入") || message.contains("支出") ||
                message.contains("花了多少钱") || message.contains("收入多少") || message.contains("支出多少");
    }

    private String generateStatisticsSummary(List<BillDTO> bills, String message) {
        if (bills.isEmpty()) {
            return "没有找到相关记录";
        }

        BigDecimal totalAmount = bills.stream()
                .map(BillDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String type = bills.get(0).getType();
        String typeText = "expense".equals(type) ? "支出" : "收入";

        // 按分类统计
        Map<String, BigDecimal> categoryStats = bills.stream()
                .collect(Collectors.groupingBy(
                        bill -> bill.getCategoryName() != null ? bill.getCategoryName() : "未知分类",
                        Collectors.reducing(BigDecimal.ZERO, BillDTO::getAmount, BigDecimal::add)));

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("您总共%s了 %.2f元\n\n", typeText, totalAmount));

        if (categoryStats.size() > 1) {
            summary.append("按分类统计：\n");
            categoryStats.entrySet().stream()
                    .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                    .forEach(entry -> {
                        String percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                                ? String.format("%.1f%%",
                                        entry.getValue().divide(totalAmount, 4, RoundingMode.HALF_UP)
                                                .multiply(new BigDecimal("100")))
                                : "0%";
                        summary.append(
                                String.format("• %s: %.2f元 (%s)\n", entry.getKey(), entry.getValue(), percentage));
                    });
        }

        summary.append(String.format("\n共%d条记录", bills.size()));

        return summary.toString();
    }

    private String extractCategoryName(String message) {
        // 匹配"删除分类XXX"格式
        Pattern pattern1 = Pattern.compile("删除分类(\\w+)");
        Matcher matcher1 = pattern1.matcher(message);
        if (matcher1.find()) {
            return matcher1.group(1);
        }

        // 匹配"把分类XXX的账单删除"格式
        Pattern pattern2 = Pattern.compile("把分类(\\w+)的账单删除");
        Matcher matcher2 = pattern2.matcher(message);
        if (matcher2.find()) {
            return matcher2.group(1);
        }

        // 匹配"删除XXX分类"格式
        Pattern pattern3 = Pattern.compile("删除(\\w+)分类");
        Matcher matcher3 = pattern3.matcher(message);
        if (matcher3.find()) {
            return matcher3.group(1);
        }

        // 匹配"分类XXX"格式
        Pattern pattern4 = Pattern.compile("分类(\\w+)");
        Matcher matcher4 = pattern4.matcher(message);
        if (matcher4.find()) {
            return matcher4.group(1);
        }

        // 匹配"删除XXX"格式（如果XXX不是数字）
        Pattern pattern5 = Pattern.compile("删除(\\w+)");
        Matcher matcher5 = pattern5.matcher(message);
        if (matcher5.find()) {
            String candidate = matcher5.group(1);
            // 检查是否是数字，如果不是数字且不是"账单"，则可能是分类名
            if (!candidate.matches("\\d+") && !candidate.contains("账单")) {
                return candidate;
            }
        }

        return null;
    }

    private String generateCreateBillResponse(BillDTO bill) {
        String typeText = "expense".equals(bill.getType()) ? "支出" : "收入";
        String amountText = "expense".equals(bill.getType()) ? "-" : "+";
        String remarksText = bill.getRemarks() != null && !bill.getRemarks().trim().isEmpty() ? bill.getRemarks()
                : "无备注";

        return String.format("✅ 账单创建成功！\n\n📋 账单详情：\n• 类型：%s\n• 金额：%s￥%.2f\n• 分类：%s\n• 备注：%s",
                typeText, amountText, bill.getAmount(),
                bill.getCategoryName() != null ? bill.getCategoryName() : "未知分类",
                remarksText);
    }

    /**
     * 判断是否为创建分类
     */
    private boolean isCreateCategory(String message) {
        return (message.contains("添加") || message.contains("创建") || message.contains("增加")) && message.contains("分类");
    }

    /**
     * 判断是否为删除分类
     */
    private boolean isDeleteCategory(String message) {
        return message.contains("删除") && message.contains("分类");
    }
}