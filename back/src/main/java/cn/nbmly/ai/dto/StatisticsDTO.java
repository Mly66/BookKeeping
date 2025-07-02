package cn.nbmly.ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsDTO {

    // 总收支统计
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netIncome;

    // 时间段统计（周、月、年）
    private BigDecimal weekIncome;
    private BigDecimal weekExpense;
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    private BigDecimal yearIncome;
    private BigDecimal yearExpense;

    // 最大和平均统计
    private BigDecimal maxIncome;
    private BigDecimal maxExpense;
    private BigDecimal avgIncome;
    private BigDecimal avgExpense;

    // 类别统计
    private List<CategoryStat> categoryStats;

    // 月度统计
    private List<MonthlyStat> monthlyStats;

    // 新增：日度统计
    private List<DailyStat> dailyStats;

    // Top类别统计
    private List<CategoryStat> topExpenseCategories;
    private List<CategoryStat> topIncomeCategories;

    @Data
    public static class CategoryStat {
        private Long categoryId;
        private String categoryName;
        private BigDecimal amount;
        private String type; // "expense" or "income"
    }

    @Data
    public static class MonthlyStat {
        private String month; // "yyyy-MM" format
        private BigDecimal income;
        private BigDecimal expense;
    }

    // 新增：日度统计内部类
    @Data
    public static class DailyStat {
        private String date; // "yyyy-MM-dd" format
        private BigDecimal income;
        private BigDecimal expense;
    }
}