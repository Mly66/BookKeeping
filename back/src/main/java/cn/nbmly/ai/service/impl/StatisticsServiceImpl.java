package cn.nbmly.ai.service.impl;

import cn.nbmly.ai.dto.StatisticsDTO;
import cn.nbmly.ai.entity.Bill;
import cn.nbmly.ai.entity.Category;
import cn.nbmly.ai.repository.BillRepository;
import cn.nbmly.ai.repository.CategoryRepository;
import cn.nbmly.ai.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final BillRepository billRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public StatisticsDTO getTotalStatistics(Long userId) {
        List<Bill> bills = billRepository.findByUserId(userId);
        StatisticsDTO stats = new StatisticsDTO();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal maxIncome = BigDecimal.ZERO;
        BigDecimal maxExpense = BigDecimal.ZERO;
        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpense = BigDecimal.ZERO;
        int countIncome = 0;
        int countExpense = 0;

        for (Bill bill : bills) {
            if ("income".equals(bill.getType())) {
                totalIncome = totalIncome.add(bill.getAmount());
                sumIncome = sumIncome.add(bill.getAmount());
                countIncome++;
                if (bill.getAmount().compareTo(maxIncome) > 0) {
                    maxIncome = bill.getAmount();
                }
            } else {
                totalExpense = totalExpense.add(bill.getAmount());
                sumExpense = sumExpense.add(bill.getAmount());
                countExpense++;
                if (bill.getAmount().compareTo(maxExpense) > 0) {
                    maxExpense = bill.getAmount();
                }
            }
        }

        stats.setTotalIncome(totalIncome);
        stats.setTotalExpense(totalExpense);
        stats.setNetIncome(totalIncome.subtract(totalExpense));
        stats.setMaxIncome(maxIncome);
        stats.setMaxExpense(maxExpense);
        stats.setAvgIncome(countIncome > 0 ? sumIncome.divide(BigDecimal.valueOf(countIncome), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        stats.setAvgExpense(
                countExpense > 0 ? sumExpense.divide(BigDecimal.valueOf(countExpense), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);

        return stats;
    }

    @Override
    public StatisticsDTO getTimeBasedStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Bill> bills = billRepository.findByUserId(userId);
        StatisticsDTO stats = new StatisticsDTO();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.minusMonths(1);
        LocalDateTime yearStart = now.minusYears(1);

        BigDecimal weekIncome = BigDecimal.ZERO;
        BigDecimal weekExpense = BigDecimal.ZERO;
        BigDecimal monthIncome = BigDecimal.ZERO;
        BigDecimal monthExpense = BigDecimal.ZERO;
        BigDecimal yearIncome = BigDecimal.ZERO;
        BigDecimal yearExpense = BigDecimal.ZERO;

        for (Bill bill : bills) {
            LocalDateTime billTime = bill.getBillTime();

            if (billTime.isAfter(weekStart)) {
                if ("income".equals(bill.getType())) {
                    weekIncome = weekIncome.add(bill.getAmount());
                } else {
                    weekExpense = weekExpense.add(bill.getAmount());
                }
            }

            if (billTime.isAfter(monthStart)) {
                if ("income".equals(bill.getType())) {
                    monthIncome = monthIncome.add(bill.getAmount());
                } else {
                    monthExpense = monthExpense.add(bill.getAmount());
                }
            }

            if (billTime.isAfter(yearStart)) {
                if ("income".equals(bill.getType())) {
                    yearIncome = yearIncome.add(bill.getAmount());
                } else {
                    yearExpense = yearExpense.add(bill.getAmount());
                }
            }
        }

        stats.setWeekIncome(weekIncome);
        stats.setWeekExpense(weekExpense);
        stats.setMonthIncome(monthIncome);
        stats.setMonthExpense(monthExpense);
        stats.setYearIncome(yearIncome);
        stats.setYearExpense(yearExpense);

        return stats;
    }

    @Override
    public StatisticsDTO getCategoryStatistics(Long userId) {
        List<Bill> bills = billRepository.findByUserId(userId);
        List<Category> categories = categoryRepository.findByUserId(userId);

        Map<Long, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        Map<Long, BigDecimal> categoryAmounts = new HashMap<>();

        for (Bill bill : bills) {
            Long categoryId = bill.getCategoryId();
            categoryAmounts.merge(categoryId, bill.getAmount(), BigDecimal::add);
        }

        List<StatisticsDTO.CategoryStat> categoryStats = categoryAmounts.entrySet().stream()
                .map(entry -> {
                    StatisticsDTO.CategoryStat stat = new StatisticsDTO.CategoryStat();
                    stat.setCategoryId(entry.getKey());
                    Category category = categoryMap.get(entry.getKey());
                    stat.setCategoryName(category != null ? category.getName() : "未知分类");
                    stat.setAmount(entry.getValue());
                    stat.setType(category != null ? category.getType() : "unknown");
                    return stat;
                })
                .collect(Collectors.toList());

        StatisticsDTO stats = new StatisticsDTO();
        stats.setCategoryStats(categoryStats);
        return stats;
    }

    @Override
    public StatisticsDTO getMonthlyStatistics(Long userId) {
        List<Bill> bills = billRepository.findByUserId(userId);
        Map<String, BigDecimal> monthlyIncome = new HashMap<>();
        Map<String, BigDecimal> monthlyExpense = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Bill bill : bills) {
            String month = bill.getBillTime().format(formatter);
            if ("income".equals(bill.getType())) {
                monthlyIncome.merge(month, bill.getAmount(), BigDecimal::add);
            } else {
                monthlyExpense.merge(month, bill.getAmount(), BigDecimal::add);
            }
        }

        Set<String> allMonths = new HashSet<>();
        allMonths.addAll(monthlyIncome.keySet());
        allMonths.addAll(monthlyExpense.keySet());

        List<StatisticsDTO.MonthlyStat> monthlyStats = allMonths.stream()
                .sorted()
                .map(month -> {
                    StatisticsDTO.MonthlyStat stat = new StatisticsDTO.MonthlyStat();
                    stat.setMonth(month);
                    stat.setIncome(monthlyIncome.getOrDefault(month, BigDecimal.ZERO));
                    stat.setExpense(monthlyExpense.getOrDefault(month, BigDecimal.ZERO));
                    return stat;
                })
                .collect(Collectors.toList());

        StatisticsDTO stats = new StatisticsDTO();
        stats.setMonthlyStats(monthlyStats);
        return stats;
    }

    @Override
    public StatisticsDTO getTopCategoryStatistics(Long userId) {
        List<Bill> bills = billRepository.findByUserId(userId);
        List<Category> categories = categoryRepository.findByUserId(userId);

        Map<Long, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        Map<Long, BigDecimal> expenseCategoryAmounts = new HashMap<>();
        Map<Long, BigDecimal> incomeCategoryAmounts = new HashMap<>();

        for (Bill bill : bills) {
            Long categoryId = bill.getCategoryId();
            if ("income".equals(bill.getType())) {
                incomeCategoryAmounts.merge(categoryId, bill.getAmount(), BigDecimal::add);
            } else {
                expenseCategoryAmounts.merge(categoryId, bill.getAmount(), BigDecimal::add);
            }
        }

        List<StatisticsDTO.CategoryStat> topExpenseCategories = expenseCategoryAmounts.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .map(entry -> {
                    StatisticsDTO.CategoryStat stat = new StatisticsDTO.CategoryStat();
                    stat.setCategoryId(entry.getKey());
                    Category category = categoryMap.get(entry.getKey());
                    stat.setCategoryName(category != null ? category.getName() : "未知分类");
                    stat.setAmount(entry.getValue());
                    stat.setType("expense");
                    return stat;
                })
                .collect(Collectors.toList());

        List<StatisticsDTO.CategoryStat> topIncomeCategories = incomeCategoryAmounts.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .map(entry -> {
                    StatisticsDTO.CategoryStat stat = new StatisticsDTO.CategoryStat();
                    stat.setCategoryId(entry.getKey());
                    Category category = categoryMap.get(entry.getKey());
                    stat.setCategoryName(category != null ? category.getName() : "未知分类");
                    stat.setAmount(entry.getValue());
                    stat.setType("income");
                    return stat;
                })
                .collect(Collectors.toList());

        StatisticsDTO stats = new StatisticsDTO();
        stats.setTopExpenseCategories(topExpenseCategories);
        stats.setTopIncomeCategories(topIncomeCategories);
        return stats;
    }

    @Override
    public StatisticsDTO getDailyStatistics(Long userId) {
        List<Bill> bills = billRepository.findByUserId(userId);
        Map<String, BigDecimal> dailyIncome = new HashMap<>();
        Map<String, BigDecimal> dailyExpense = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Bill bill : bills) {
            String day = bill.getBillTime().format(formatter);
            if ("income".equals(bill.getType())) {
                dailyIncome.merge(day, bill.getAmount(), BigDecimal::add);
            } else {
                dailyExpense.merge(day, bill.getAmount(), BigDecimal::add);
            }
        }

        Set<String> allDays = new HashSet<>();
        allDays.addAll(dailyIncome.keySet());
        allDays.addAll(dailyExpense.keySet());

        List<StatisticsDTO.DailyStat> dailyStats = allDays.stream()
                .sorted()
                .map(day -> {
                    StatisticsDTO.DailyStat stat = new StatisticsDTO.DailyStat();
                    stat.setDate(day);
                    stat.setIncome(dailyIncome.getOrDefault(day, BigDecimal.ZERO));
                    stat.setExpense(dailyExpense.getOrDefault(day, BigDecimal.ZERO));
                    return stat;
                })
                .collect(Collectors.toList());

        StatisticsDTO stats = new StatisticsDTO();
        stats.setDailyStats(dailyStats);
        return stats;
    }
}