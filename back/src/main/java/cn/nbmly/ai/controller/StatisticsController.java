package cn.nbmly.ai.controller;

import cn.nbmly.ai.dto.StatisticsDTO;
import cn.nbmly.ai.entity.User;
import cn.nbmly.ai.service.StatisticsService;
import cn.nbmly.ai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    @GetMapping("/total")
    public ResponseEntity<StatisticsDTO> getTotalStatistics(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        StatisticsDTO stats = statisticsService.getTotalStatistics(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/time-based")
    public ResponseEntity<StatisticsDTO> getTimeBasedStatistics(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        StatisticsDTO stats = statisticsService.getTimeBasedStatistics(currentUser.getId(), null, null);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/category")
    public ResponseEntity<StatisticsDTO> getCategoryStatistics(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        StatisticsDTO stats = statisticsService.getCategoryStatistics(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/monthly")
    public ResponseEntity<StatisticsDTO> getMonthlyStatistics(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        StatisticsDTO stats = statisticsService.getMonthlyStatistics(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top-categories")
    public ResponseEntity<StatisticsDTO> getTopCategoryStatistics(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        StatisticsDTO stats = statisticsService.getTopCategoryStatistics(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/all")
    public ResponseEntity<StatisticsDTO> getAllStatistics(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        // 获取所有统计数据
        StatisticsDTO totalStats = statisticsService.getTotalStatistics(currentUser.getId());
        StatisticsDTO timeStats = statisticsService.getTimeBasedStatistics(currentUser.getId(), null, null);
        StatisticsDTO topStats = statisticsService.getTopCategoryStatistics(currentUser.getId());

        // 合并统计数据
        totalStats.setWeekIncome(timeStats.getWeekIncome());
        totalStats.setWeekExpense(timeStats.getWeekExpense());
        totalStats.setMonthIncome(timeStats.getMonthIncome());
        totalStats.setMonthExpense(timeStats.getMonthExpense());
        totalStats.setYearIncome(timeStats.getYearIncome());
        totalStats.setYearExpense(timeStats.getYearExpense());
        totalStats.setTopExpenseCategories(topStats.getTopExpenseCategories());
        totalStats.setTopIncomeCategories(topStats.getTopIncomeCategories());

        return ResponseEntity.ok(totalStats);
    }

    @GetMapping("/daily")
    public ResponseEntity<StatisticsDTO> getDailyStatistics(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        StatisticsDTO stats = statisticsService.getDailyStatistics(currentUser.getId());
        return ResponseEntity.ok(stats);
    }
}