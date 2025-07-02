package cn.nbmly.ai.service.impl;

import cn.nbmly.ai.dto.BillDTO;
import cn.nbmly.ai.dto.BillRequest;
import cn.nbmly.ai.dto.CategoryDTO;
import cn.nbmly.ai.entity.Bill;
import cn.nbmly.ai.entity.Category;
import cn.nbmly.ai.repository.BillRepository;
import cn.nbmly.ai.repository.CategoryRepository;
import cn.nbmly.ai.service.AiBillService;
import cn.nbmly.ai.service.BillService;
import cn.nbmly.ai.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiBillServiceImpl implements AiBillService {

    private final BillService billService;
    private final CategoryService categoryService;
    private final BillRepository billRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public BillDTO createBill(String description, Long userId) {
        BillRequest billRequest = parseBillDescription(description, userId);
        return billService.createBill(billRequest, userId);
    }

    @Override
    public List<BillDTO> queryBills(String query, Long userId) {
        // 如果查询条件为空，返回所有账单
        if (query == null || query.trim().isEmpty()) {
            return billService.getBillsByUser(userId);
        }
        
        if (query.contains("所有") || query.contains("全部")) {
            return billService.getBillsByUser(userId);
        }

        // 处理统计查询
        if (isStatisticsQuery(query)) {
            return handleStatisticsQuery(query, userId);
        }

        // 根据关键词过滤账单
        List<BillDTO> allBills = billService.getBillsByUser(userId);
        return allBills.stream()
                .filter(bill -> matchesQuery(bill, query))
                .collect(Collectors.toList());
    }

    @Override
    public BillDTO updateBill(Long billId, String description, Long userId) {
        BillRequest billRequest = parseBillDescription(description, userId);
        return billService.updateBill(billId, billRequest, userId);
    }

    @Override
    public void deleteBill(Long billId, Long userId) {
        billService.deleteBill(billId, userId);
    }

    @Override
    public List<CategoryDTO> getUserCategories(Long userId) {
        return categoryService.getCategoriesByUser(userId);
    }

    @Override
    public String getBillStatistics(Long userId) {
        List<BillDTO> bills = billService.getBillsByUser(userId);

        BigDecimal totalIncome = bills.stream()
                .filter(bill -> "income".equals(bill.getType()))
                .map(BillDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = bills.stream()
                .filter(bill -> "expense".equals(bill.getType()))
                .map(BillDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return String.format("账单统计：\n总收入：%.2f元\n总支出：%.2f元\n余额：%.2f元\n账单总数：%d条",
                totalIncome, totalExpense, balance, bills.size());
    }

    @Override
    public List<BillDTO> deleteBillsByCategory(String categoryName, Long userId) {
        // 先找到该分类
        List<CategoryDTO> categories = categoryService.getCategoriesByUser(userId);
        CategoryDTO targetCategory = categories.stream()
                .filter(cat -> cat.getName().equals(categoryName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到分类：" + categoryName));

        // 获取该分类的所有账单
        List<BillDTO> billsToDelete = billService.getBillsByUser(userId).stream()
                .filter(bill -> targetCategory.getId().equals(bill.getCategoryId()))
                .collect(Collectors.toList());

        // 删除所有相关账单
        for (BillDTO bill : billsToDelete) {
            billService.deleteBill(bill.getId(), userId);
        }

        return billsToDelete;
    }

    @Override
    public List<BillDTO> queryBillsByCategory(String categoryName, Long userId) {
        // 先找到该分类
        List<CategoryDTO> categories = categoryService.getCategoriesByUser(userId);
        CategoryDTO targetCategory = categories.stream()
                .filter(cat -> cat.getName().equals(categoryName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到分类：" + categoryName));

        // 获取该分类的所有账单
        return billService.getBillsByUser(userId).stream()
                .filter(bill -> targetCategory.getId().equals(bill.getCategoryId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BillDTO> queryBillsByTimeRange(String startDate, String endDate, Long userId) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
        return billService.getBillsByUser(userId).stream()
            .filter(bill -> {
                LocalDateTime billTime = bill.getBillTime();
                return !billTime.isBefore(start) && !billTime.isAfter(end);
            })
            .collect(Collectors.toList());
    }

    private BillRequest parseBillDescription(String description, Long userId) {
        BillRequest billRequest = new BillRequest();

        // 解析金额
        BigDecimal amount = extractAmount(description);
        billRequest.setAmount(amount);

        // 解析类型（收入/支出）
        String type = extractType(description);
        billRequest.setType(type);

        // 解析分类
        Long categoryId = extractCategory(description, userId, type);
        billRequest.setCategoryId(categoryId);

        // 解析备注
        String remarks = extractRemarks(description);
        billRequest.setRemarks(remarks);

        // 解析时间
        LocalDateTime billTime = extractTime(description);
        billRequest.setBillTime(billTime);

        return billRequest;
    }

    private BigDecimal extractAmount(String description) {
        // 匹配金额模式：数字+元/块/块钱
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*(元|块|块钱|RMB|¥)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }

        // 匹配纯数字
        pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
        matcher = pattern.matcher(description);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }

        throw new RuntimeException("无法解析金额，请明确指定金额，例如：100元");
    }

    private String extractType(String description) {
        if (description.contains("收入") || description.contains("赚") || description.contains("工资") ||
                description.contains("奖金") || description.contains("投资") || description.contains("理财")) {
            return "income";
        }
        return "expense"; // 默认为支出
    }

    private Long extractCategory(String description, Long userId, String type) {
        List<CategoryDTO> categories = categoryService.getCategoriesByUser(userId);
        Map<String, Long> categoryMap = categories.stream()
                .filter(cat -> cat.getType().equals(type))
                .collect(Collectors.toMap(CategoryDTO::getName, CategoryDTO::getId));

        // 根据描述匹配分类
        for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
            if (description.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 如果没有匹配到，返回第一个默认分类
        return categories.stream()
                .filter(cat -> cat.getType().equals(type))
                .findFirst()
                .map(CategoryDTO::getId)
                .orElseThrow(() -> new RuntimeException("未找到合适的分类，请先创建分类"));
    }

    private String extractRemarks(String description) {
        // 提取备注信息，去除金额、分类等关键词
        String remarks = description;

        // 移除金额
        remarks = remarks.replaceAll("\\d+(\\.\\d+)?\\s*(元|块|块钱|RMB|¥)", "");

        // 移除时间关键词
        remarks = remarks.replaceAll("今天|昨天|前天", "");

        // 移除常见关键词
        String[] keywords = { "收入", "支出", "消费", "购买", "支付", "转账", "工资", "奖金", "投资", "理财", "花了", "赚了" };
        for (String keyword : keywords) {
            remarks = remarks.replace(keyword, "");
        }

        // 清理多余空格
        remarks = remarks.replaceAll("\\s+", " ").trim();

        // 如果备注为空或太短，生成默认备注
        if (remarks.isEmpty() || remarks.length() < 2) {
            if (description.contains("买")) {
                // 提取购买的商品
                Pattern buyPattern = Pattern.compile("买(\\w+)");
                Matcher buyMatcher = buyPattern.matcher(description);
                if (buyMatcher.find()) {
                    return "购买" + buyMatcher.group(1);
                }
                return "购物消费";
            } else if (description.contains("工资")) {
                return "工资收入";
            } else if (description.contains("吃饭") || description.contains("午餐") || description.contains("晚餐")) {
                return "餐饮消费";
            } else if (description.contains("打车") || description.contains("交通")) {
                return "交通费用";
            } else {
                return "日常消费";
            }
        }

        return remarks;
    }

    private LocalDateTime extractTime(String description) {
        // 解析时间，支持多种格式
        if (description.contains("今天")) {
            return LocalDateTime.now();
        } else if (description.contains("昨天")) {
            return LocalDateTime.now().minusDays(1);
        } else if (description.contains("前天")) {
            return LocalDateTime.now().minusDays(2);
        }

        // 尝试解析具体日期
        Pattern pattern = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            return LocalDateTime.of(year, month, day, 0, 0);
        }

        // 默认为当前时间
        return LocalDateTime.now();
    }

    private boolean matchesQuery(BillDTO bill, String query) {
        String lowerQuery = query.toLowerCase();

        // 按类型过滤
        if (lowerQuery.contains("收入") && !"income".equals(bill.getType())) {
            return false;
        }
        if (lowerQuery.contains("支出") && !"expense".equals(bill.getType())) {
            return false;
        }

        // 按分类过滤
        if (bill.getCategoryName() != null && lowerQuery.contains(bill.getCategoryName().toLowerCase())) {
            return true;
        }

        // 按备注过滤
        if (bill.getRemarks() != null && bill.getRemarks().toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // 按金额过滤
        if (lowerQuery.contains("大于") || lowerQuery.contains("超过")) {
            BigDecimal amount = extractAmount(query);
            return bill.getAmount().compareTo(amount) > 0;
        }
        if (lowerQuery.contains("小于") || lowerQuery.contains("低于")) {
            BigDecimal amount = extractAmount(query);
            return bill.getAmount().compareTo(amount) < 0;
        }

        return false;
    }

    private boolean isStatisticsQuery(String query) {
        return query.contains("多少") || query.contains("花了") || query.contains("收入了") ||
                query.contains("支出") || query.contains("收入");
    }

    private List<BillDTO> handleStatisticsQuery(String query, Long userId) {
        List<BillDTO> allBills = billService.getBillsByUser(userId);

        // 处理时间过滤
        if (query.contains("今天")) {
            final LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            final LocalDateTime endTime = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59)
                    .withNano(999999999);
            allBills = allBills.stream()
                    .filter(bill -> {
                        LocalDateTime billTime = LocalDateTime
                                .parse(bill.getBillTime().toString().replace("T", " ").substring(0, 19));
                        return !billTime.isBefore(startTime) && !billTime.isAfter(endTime);
                    })
                    .collect(Collectors.toList());
        } else if (query.contains("昨天")) {
            final LocalDateTime startTime = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0)
                    .withNano(0);
            final LocalDateTime endTime = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59)
                    .withNano(999999999);
            allBills = allBills.stream()
                    .filter(bill -> {
                        LocalDateTime billTime = LocalDateTime
                                .parse(bill.getBillTime().toString().replace("T", " ").substring(0, 19));
                        return !billTime.isBefore(startTime) && !billTime.isAfter(endTime);
                    })
                    .collect(Collectors.toList());
        } else if (query.contains("前天")) {
            final LocalDateTime startTime = LocalDateTime.now().minusDays(2).withHour(0).withMinute(0).withSecond(0)
                    .withNano(0);
            final LocalDateTime endTime = LocalDateTime.now().minusDays(2).withHour(23).withMinute(59).withSecond(59)
                    .withNano(999999999);
            allBills = allBills.stream()
                    .filter(bill -> {
                        LocalDateTime billTime = LocalDateTime
                                .parse(bill.getBillTime().toString().replace("T", " ").substring(0, 19));
                        return !billTime.isBefore(startTime) && !billTime.isAfter(endTime);
                    })
                    .collect(Collectors.toList());
        }

        // 如果查询包含"花了多少钱"，返回所有支出
        if (query.contains("花了") || query.contains("支出")) {
            allBills = allBills.stream()
                    .filter(bill -> "expense".equals(bill.getType()))
                    .collect(Collectors.toList());
        }

        // 如果查询包含"收入多少"，返回所有收入
        if (query.contains("收入了") || query.contains("收入")) {
            allBills = allBills.stream()
                    .filter(bill -> "income".equals(bill.getType()))
                    .collect(Collectors.toList());
        }

        // 如果查询包含分类关键词，按分类过滤
        List<CategoryDTO> categories = categoryService.getCategoriesByUser(userId);
        for (CategoryDTO category : categories) {
            if (query.contains(category.getName())) {
                final Long categoryId = category.getId();
                allBills = allBills.stream()
                        .filter(bill -> categoryId.equals(bill.getCategoryId()))
                        .collect(Collectors.toList());
                break; // 找到第一个匹配的分类就停止
            }
        }

        // 特殊处理餐饮相关查询
        if (query.contains("吃饭") || query.contains("餐饮") || query.contains("午餐") || query.contains("晚餐")) {
            allBills = allBills.stream()
                    .filter(bill -> {
                        String categoryName = bill.getCategoryName();
                        String remarks = bill.getRemarks();
                        return (categoryName != null && (categoryName.contains("餐饮") || categoryName.contains("吃饭"))) ||
                                (remarks != null && (remarks.contains("餐饮") || remarks.contains("吃饭") ||
                                        remarks.contains("午餐") || remarks.contains("晚餐")));
                    })
                    .collect(Collectors.toList());
        }

        return allBills;
    }
}