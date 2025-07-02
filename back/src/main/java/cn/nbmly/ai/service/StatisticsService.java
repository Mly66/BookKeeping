package cn.nbmly.ai.service;

import cn.nbmly.ai.dto.StatisticsDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {

    /**
     * 获取用户的总收支统计
     */
    StatisticsDTO getTotalStatistics(Long userId);

    /**
     * 获取用户按时间段的统计（周、月、年）
     */
    StatisticsDTO getTimeBasedStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户按类别的统计
     */
    StatisticsDTO getCategoryStatistics(Long userId);

    /**
     * 获取用户按月份的统计
     */
    StatisticsDTO getMonthlyStatistics(Long userId);

    /**
     * 获取用户的Top类别统计
     */
    StatisticsDTO getTopCategoryStatistics(Long userId);

    /**
     * 获取用户按日期（日）统计
     */
    StatisticsDTO getDailyStatistics(Long userId);
}