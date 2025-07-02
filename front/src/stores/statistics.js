import { defineStore } from 'pinia';
import { ref } from 'vue';
import apiClient from '@/api';

export const useStatisticsStore = defineStore('statistics', () => {
  const statistics = ref(null);
  const loading = ref(false);
  const error = ref(null);

  const fetchStatistics = async () => {
    loading.value = true;
    error.value = null;
    try {
      const response = await apiClient.get('/statistics/all');
      statistics.value = response.data;
    } catch (err) {
      error.value = err.response?.data?.message || '获取统计数据失败';
      console.error('获取统计数据失败:', err);
    } finally {
      loading.value = false;
    }
  };

  const fetchTotalStatistics = async () => {
    try {
      const response = await apiClient.get('/statistics/total');
      return response.data;
    } catch (err) {
      console.error('获取总统计数据失败:', err);
      throw err;
    }
  };

  const fetchCategoryStatistics = async () => {
    try {
      const response = await apiClient.get('/statistics/category');
      return response.data;
    } catch (err) {
      console.error('获取类别统计数据失败:', err);
      throw err;
    }
  };

  const fetchMonthlyStatistics = async () => {
    try {
      const response = await apiClient.get('/statistics/monthly');
      return response.data;
    } catch (err) {
      console.error('获取月度统计数据失败:', err);
      throw err;
    }
  };

  const fetchTopCategoryStatistics = async () => {
    try {
      const response = await apiClient.get('/statistics/top-categories');
      return response.data;
    } catch (err) {
      console.error('获取Top类别统计数据失败:', err);
      throw err;
    }
  };

  const fetchDailyStatistics = async () => {
    try {
      const response = await apiClient.get('/statistics/daily');
      return response.data;
    } catch (err) {
      console.error('获取日度统计数据失败:', err);
      throw err;
    }
  };

  return {
    statistics,
    loading,
    error,
    fetchStatistics,
    fetchTotalStatistics,
    fetchCategoryStatistics,
    fetchMonthlyStatistics,
    fetchTopCategoryStatistics,
    fetchDailyStatistics
  };
}); 