package cn.nbmly.ai.services;

import cn.nbmly.ai.dto.BillDTO;
import cn.nbmly.ai.dto.CategoryDTO;
import cn.nbmly.ai.dto.CategoryRequest;
import cn.nbmly.ai.dto.UserDTO;
import cn.nbmly.ai.entity.User;
import cn.nbmly.ai.service.AiBillService;
import cn.nbmly.ai.service.CategoryService;
import cn.nbmly.ai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class BillTools {
    private final AiBillService aiBillService;
    private final UserService userService;
    private final CategoryService categoryService;
    
    // 当前用户ID，在工具调用时设置
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    /**
     * 设置当前用户ID（在工具调用前调用）
     */
    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = currentUserId.get();
        if (userId == null) {
            // 尝试从SecurityContext获取
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                // 这里需要根据实际情况获取用户ID
                // 暂时返回默认值
                userId = 8L; // 默认用户ID
            }
        }
        return userId;
    }

    /**
     * 获取当前用户信息
     */
    @Tool(description = "获取当前用户信息")
    public UserDTO getCurrentUser() {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.getCurrentUser 被调用 ===");
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.getCurrentUser 被调用 ===");
            System.out.println("用户ID: " + userId);
            
            // 通过UserService获取真实用户信息
            User user = userService.findById(userId);
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            userDTO.setEmail(user.getEmail());
            userDTO.setPhone(user.getPhone());
            userDTO.setAvatar(user.getAvatar());
            userDTO.setCreateTime(user.getCreateTime());
            
            log.info("获取用户信息成功: {}", userDTO);
            System.out.println("获取用户信息成功: " + userDTO);
            return userDTO;
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            System.out.println("获取用户信息失败: " + e.getMessage());
            throw new RuntimeException("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 创建账单
     */
    @Tool(description = "根据描述创建账单")
    public BillDTO createBill(
            @ToolParam(description = "账单描述，如'今天花了100元买午餐'") String description) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.createBill 被调用 ===");
            log.info("描述: {}", description);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.createBill 被调用 ===");
            System.out.println("描述: " + description);
            System.out.println("用户ID: " + userId);
            
            BillDTO result = aiBillService.createBill(description, userId);
            log.info("创建账单成功: {}", result);
            System.out.println("创建账单成功: " + result);
            return result;
        } catch (Exception e) {
            log.error("创建账单失败", e);
            System.out.println("创建账单失败: " + e.getMessage());
            throw new RuntimeException("创建账单失败：" + e.getMessage());
        }
    }

    /**
     * 查询账单
     */
    @Tool(description = "根据查询条件查询账单")
    public List<BillDTO> queryBills(
            @ToolParam(description = "查询条件，如'餐饮支出'", required = false) String query) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.queryBills 被调用 ===");
            log.info("查询条件: {}", query);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.queryBills 被调用 ===");
            System.out.println("查询条件: " + query);
            System.out.println("用户ID: " + userId);
            
            List<BillDTO> result = aiBillService.queryBills(query, userId);
            log.info("查询账单成功，共{}条", result.size());
            System.out.println("查询账单成功，共" + result.size() + "条");
            return result;
        } catch (Exception e) {
            log.error("查询账单失败", e);
            System.out.println("查询账单失败: " + e.getMessage());
            throw new RuntimeException("查询账单失败：" + e.getMessage());
        }
    }

    /**
     * 更新账单
     */
    @Tool(description = "根据账单ID和描述更新账单")
    public BillDTO updateBill(
            @ToolParam(description = "账单ID") Long billId,
            @ToolParam(description = "新的账单描述") String description) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.updateBill 被调用 ===");
            log.info("账单ID: {}", billId);
            log.info("描述: {}", description);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.updateBill 被调用 ===");
            System.out.println("账单ID: " + billId);
            System.out.println("描述: " + description);
            System.out.println("用户ID: " + userId);
            
            BillDTO result = aiBillService.updateBill(billId, description, userId);
            log.info("更新账单成功: {}", result);
            System.out.println("更新账单成功: " + result);
            return result;
        } catch (Exception e) {
            log.error("更新账单失败", e);
            System.out.println("更新账单失败: " + e.getMessage());
            throw new RuntimeException("更新账单失败：" + e.getMessage());
        }
    }

    /**
     * 删除账单
     */
    @Tool(description = "根据账单ID删除账单")
    public String deleteBill(
            @ToolParam(description = "账单ID") Long billId) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.deleteBill 被调用 ===");
            log.info("账单ID: {}", billId);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.deleteBill 被调用 ===");
            System.out.println("账单ID: " + billId);
            System.out.println("用户ID: " + userId);
            
            aiBillService.deleteBill(billId, userId);
            String result = "账单删除成功";
            log.info("删除账单成功");
            System.out.println("删除账单成功");
            return result;
        } catch (Exception e) {
            log.error("删除账单失败", e);
            System.out.println("删除账单失败: " + e.getMessage());
            throw new RuntimeException("删除账单失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户分类
     */
    @Tool(description = "获取当前用户的所有账单分类")
    public List<CategoryDTO> getUserCategories() {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.getUserCategories 被调用 ===");
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.getUserCategories 被调用 ===");
            System.out.println("用户ID: " + userId);
            
            List<CategoryDTO> result = aiBillService.getUserCategories(userId);
            log.info("获取用户分类成功，共{}个", result.size());
            System.out.println("获取用户分类成功，共" + result.size() + "个");
            return result;
        } catch (Exception e) {
            log.error("获取分类失败", e);
            System.out.println("获取分类失败: " + e.getMessage());
            throw new RuntimeException("获取分类失败：" + e.getMessage());
        }
    }

    /**
     * 获取账单统计
     */
    @Tool(description = "获取当前用户的账单统计信息")
    public String getBillStatistics() {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.getBillStatistics 被调用 ===");
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.getBillStatistics 被调用 ===");
            System.out.println("用户ID: " + userId);
            
            String result = aiBillService.getBillStatistics(userId);
            log.info("获取账单统计成功: {}", result);
            System.out.println("获取账单统计成功: " + result);
            return result;
        } catch (Exception e) {
            log.error("获取统计失败", e);
            System.out.println("获取统计失败: " + e.getMessage());
            throw new RuntimeException("获取统计失败：" + e.getMessage());
        }
    }

    /**
     * 创建分类
     */
    @Tool(description = "创建新的账单分类")
    public CategoryDTO createCategory(
            @ToolParam(description = "分类名称") String name,
            @ToolParam(description = "分类类型：income(收入) 或 expense(支出)") String type) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.createCategory 被调用 ===");
            log.info("分类名称: {}", name);
            log.info("分类类型: {}", type);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.createCategory 被调用 ===");
            System.out.println("分类名称: " + name);
            System.out.println("分类类型: " + type);
            System.out.println("用户ID: " + userId);
            
            CategoryRequest categoryRequest = new CategoryRequest();
            categoryRequest.setName(name);
            categoryRequest.setType(type);
            
            CategoryDTO result = categoryService.createCategory(categoryRequest, userId);
            log.info("创建分类成功: {}", result);
            System.out.println("创建分类成功: " + result);
            return result;
        } catch (Exception e) {
            log.error("创建分类失败", e);
            System.out.println("创建分类失败: " + e.getMessage());
            throw new RuntimeException("创建分类失败：" + e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @Tool(description = "删除指定的账单分类")
    public String deleteCategory(
            @ToolParam(description = "分类名称") String categoryName) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.deleteCategory 被调用 ===");
            log.info("分类名称: {}", categoryName);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.deleteCategory 被调用 ===");
            System.out.println("分类名称: " + categoryName);
            System.out.println("用户ID: " + userId);
            
            // 先删除该分类下的所有账单
            List<BillDTO> deletedBills = aiBillService.deleteBillsByCategory(categoryName, userId);
            
            // 再删除分类本身
            List<CategoryDTO> categories = categoryService.getCategoriesByUser(userId);
            CategoryDTO targetCat = categories.stream()
                    .filter(cat -> categoryName.equals(cat.getName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到分类:" + categoryName));
            
            categoryService.deleteCategory(targetCat.getId(), userId);
            
            String result = String.format("已删除分类 '%s' 以及相关账单 %d 条", categoryName, deletedBills.size());
            log.info("删除分类成功: {}", result);
            System.out.println("删除分类成功: " + result);
            return result;
        } catch (Exception e) {
            log.error("删除分类失败", e);
            System.out.println("删除分类失败: " + e.getMessage());
            throw new RuntimeException("删除分类失败：" + e.getMessage());
        }
    }

    /**
     * 更新分类
     */
    @Tool(description = "更新账单分类信息")
    public CategoryDTO updateCategory(
            @ToolParam(description = "分类ID") Long categoryId,
            @ToolParam(description = "新的分类名称") String newName,
            @ToolParam(description = "新的分类类型：income(收入) 或 expense(支出)", required = false) String newType) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.updateCategory 被调用 ===");
            log.info("分类ID: {}", categoryId);
            log.info("新名称: {}", newName);
            log.info("新类型: {}", newType);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.updateCategory 被调用 ===");
            System.out.println("分类ID: " + categoryId);
            System.out.println("新名称: " + newName);
            System.out.println("新类型: " + newType);
            System.out.println("用户ID: " + userId);
            
            CategoryRequest categoryRequest = new CategoryRequest();
            categoryRequest.setName(newName);
            if (newType != null) {
                categoryRequest.setType(newType);
            }
            
            CategoryDTO result = categoryService.updateCategory(categoryId, categoryRequest, userId);
            log.info("更新分类成功: {}", result);
            System.out.println("更新分类成功: " + result);
            return result;
        } catch (Exception e) {
            log.error("更新分类失败", e);
            System.out.println("更新分类失败: " + e.getMessage());
            throw new RuntimeException("更新分类失败：" + e.getMessage());
        }
    }

    /**
     * 查询特定分类的账单
     */
    @Tool(description = "查询指定分类的所有账单")
    public List<BillDTO> queryBillsByCategory(
            @ToolParam(description = "分类名称") String categoryName) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.queryBillsByCategory 被调用 ===");
            log.info("分类名称: {}", categoryName);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.queryBillsByCategory 被调用 ===");
            System.out.println("分类名称: " + categoryName);
            System.out.println("用户ID: " + userId);
            
            List<BillDTO> result = aiBillService.queryBillsByCategory(categoryName, userId);
            log.info("查询分类账单成功，共{}条", result.size());
            System.out.println("查询分类账单成功，共" + result.size() + "条");
            return result;
        } catch (Exception e) {
            log.error("查询分类账单失败", e);
            System.out.println("查询分类账单失败: " + e.getMessage());
            throw new RuntimeException("查询分类账单失败：" + e.getMessage());
        }
    }

    /**
     * 按时间范围查询账单
     */
    @Tool(description = "按时间范围查询账单，开始和结束日期必须为yyyy-MM-dd格式。请AI先将自然语言时间表达转换为标准日期后再调用本方法。")
    public List<BillDTO> queryBillsByTimeRange(
            @ToolParam(description = "开始日期，格式：yyyy-MM-dd") String startDate,
            @ToolParam(description = "结束日期，格式：yyyy-MM-dd") String endDate) {
        try {
            Long userId = getCurrentUserId();
            if (startDate == null || endDate == null) {
                throw new RuntimeException("开始日期和结束日期不能为空");
            }
            return aiBillService.queryBillsByTimeRange(startDate, endDate, userId);
        } catch (Exception e) {
            throw new RuntimeException("按时间范围查询账单失败：" + e.getMessage());
        }
    }

    /**
     * 按金额范围查询账单
     */
    @Tool(description = "按金额范围查询账单")
    public List<BillDTO> queryBillsByAmountRange(
            @ToolParam(description = "最小金额") Double minAmount,
            @ToolParam(description = "最大金额") Double maxAmount) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.queryBillsByAmountRange 被调用 ===");
            log.info("最小金额: {}", minAmount);
            log.info("最大金额: {}", maxAmount);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.queryBillsByAmountRange 被调用 ===");
            System.out.println("最小金额: " + minAmount);
            System.out.println("最大金额: " + maxAmount);
            System.out.println("用户ID: " + userId);
            
            String query = String.format("金额范围：%.2f 到 %.2f", minAmount, maxAmount);
            List<BillDTO> result = aiBillService.queryBills(query, userId);
            log.info("按金额范围查询账单成功，共{}条", result.size());
            System.out.println("按金额范围查询账单成功，共" + result.size() + "条");
            return result;
        } catch (Exception e) {
            log.error("按金额范围查询账单失败", e);
            System.out.println("按金额范围查询账单失败: " + e.getMessage());
            throw new RuntimeException("按金额范围查询账单失败：" + e.getMessage());
        }
    }

    /**
     * 获取分类详情
     */
    @Tool(description = "获取指定分类的详细信息")
    public CategoryDTO getCategoryDetail(
            @ToolParam(description = "分类名称") String categoryName) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.getCategoryDetail 被调用 ===");
            log.info("分类名称: {}", categoryName);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.getCategoryDetail 被调用 ===");
            System.out.println("分类名称: " + categoryName);
            System.out.println("用户ID: " + userId);
            
            List<CategoryDTO> categories = categoryService.getCategoriesByUser(userId);
            CategoryDTO result = categories.stream()
                    .filter(cat -> categoryName.equals(cat.getName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到分类：" + categoryName));
            
            log.info("获取分类详情成功: {}", result);
            System.out.println("获取分类详情成功: " + result);
            return result;
        } catch (Exception e) {
            log.error("获取分类详情失败", e);
            System.out.println("获取分类详情失败: " + e.getMessage());
            throw new RuntimeException("获取分类详情失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除账单
     */
    @Tool(description = "批量删除指定条件的账单")
    public String batchDeleteBills(
            @ToolParam(description = "删除条件，如'餐饮支出'") String condition) {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.batchDeleteBills 被调用 ===");
            log.info("删除条件: {}", condition);
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.batchDeleteBills 被调用 ===");
            System.out.println("删除条件: " + condition);
            System.out.println("用户ID: " + userId);
            
            List<BillDTO> billsToDelete = aiBillService.queryBills(condition, userId);
            int count = 0;
            for (BillDTO bill : billsToDelete) {
                aiBillService.deleteBill(bill.getId(), userId);
                count++;
            }
            
            String result = String.format("批量删除成功，共删除 %d 条账单", count);
            log.info("批量删除账单成功: {}", result);
            System.out.println("批量删除账单成功: " + result);
            return result;
        } catch (Exception e) {
            log.error("批量删除账单失败", e);
            System.out.println("批量删除账单失败: " + e.getMessage());
            throw new RuntimeException("批量删除账单失败：" + e.getMessage());
        }
    }

    /**
     * 获取收入统计
     */
    @Tool(description = "获取收入统计信息")
    public String getIncomeStatistics() {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.getIncomeStatistics 被调用 ===");
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.getIncomeStatistics 被调用 ===");
            System.out.println("用户ID: " + userId);
            
            List<BillDTO> bills = aiBillService.queryBills("收入", userId);
            double totalIncome = bills.stream()
                    .mapToDouble(bill -> bill.getAmount().doubleValue())
                    .sum();
            
            String result = String.format("收入统计：\n总收入：%.2f元\n收入笔数：%d笔", totalIncome, bills.size());
            log.info("获取收入统计成功: {}", result);
            System.out.println("获取收入统计成功: " + result);
            return result;
        } catch (Exception e) {
            log.error("获取收入统计失败", e);
            System.out.println("获取收入统计失败: " + e.getMessage());
            throw new RuntimeException("获取收入统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取支出统计
     */
    @Tool(description = "获取支出统计信息")
    public String getExpenseStatistics() {
        try {
            Long userId = getCurrentUserId();
            log.info("=== BillTools.getExpenseStatistics 被调用 ===");
            log.info("用户ID: {}", userId);
            System.out.println("=== BillTools.getExpenseStatistics 被调用 ===");
            System.out.println("用户ID: " + userId);
            
            List<BillDTO> bills = aiBillService.queryBills("支出", userId);
            double totalExpense = bills.stream()
                    .mapToDouble(bill -> bill.getAmount().doubleValue())
                    .sum();
            
            String result = String.format("支出统计：\n总支出：%.2f元\n支出笔数：%d笔", totalExpense, bills.size());
            log.info("获取支出统计成功: {}", result);
            System.out.println("获取支出统计成功: " + result);
            return result;
        } catch (Exception e) {
            log.error("获取支出统计失败", e);
            System.out.println("获取支出统计失败: " + e.getMessage());
            throw new RuntimeException("获取支出统计失败：" + e.getMessage());
        }
    }
} 