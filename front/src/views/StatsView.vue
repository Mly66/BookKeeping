<template>
  <div class="statistics-view">
    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="statistics-header">
      <el-col :span="8">
        <el-card class="stat-card net-card">
          <div class="stat-title">净收入</div>
          <div class="stat-value net">￥{{ formatAmount(statistics?.netIncome || 0) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card income-card">
          <div class="stat-title">总收入</div>
          <div class="stat-value income">+￥{{ formatAmount(statistics?.totalIncome || 0) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card expense-card">
          <div class="stat-title">总支出</div>
          <div class="stat-value expense">-￥{{ formatAmount(statistics?.totalExpense || 0) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="statistics-main">
      <el-col :span="8">
        <el-card class="info-card">
          <template #header>Top类别</template>
          <div class="info-item">
            <span>支出Top3：</span>
            <div class="top-categories">
              <span v-for="(cat, index) in statistics?.topExpenseCategories || []" :key="index" class="category-tag">
                <el-icon><i class="el-icon-minus" /></el-icon>
                {{ cat.categoryName }}
                <span class="cat-amount">({{ formatAmount(cat.amount) }})</span>
                <el-progress :percentage="getCategoryPercent(cat.amount, 'expense')" :stroke-width="8" status="exception" style="width: 60px; margin-left: 8px;" />
              </span>
            </div>
          </div>
          <div class="info-item">
            <span>收入Top3：</span>
            <div class="top-categories">
              <span v-for="(cat, index) in statistics?.topIncomeCategories || []" :key="index" class="category-tag income">
                <el-icon><i class="el-icon-plus" /></el-icon>
                {{ cat.categoryName }}
                <span class="cat-amount">({{ formatAmount(cat.amount) }})</span>
                <el-progress :percentage="getCategoryPercent(cat.amount, 'income')" :stroke-width="8" status="success" style="width: 60px; margin-left: 8px;" />
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>数据图表</span>
              <el-radio-group v-model="chartType" @change="handleChartTypeChange" size="small">
                <el-radio-button value="pie">类别分布</el-radio-button>
                <el-radio-button value="bar">月度对比</el-radio-button>
                <el-radio-button value="line">收支趋势</el-radio-button>
              </el-radio-group>
              <el-button type="primary" @click="refreshData" :loading="loading" size="small" style="margin-left: 12px;">刷新</el-button>
            </div>
          </template>
          <div class="chart-container">
            <div v-show="chartType === 'pie'" class="pie-chart-container">
              <div ref="pieChartRef" class="chart"></div>
            </div>
            <div v-show="chartType === 'bar'" class="bar-chart-container">
              <div ref="barChartRef" class="chart"></div>
            </div>
            <div v-show="chartType === 'line'" class="line-chart-container">
              <div ref="lineChartRef" class="chart"></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import { useStatisticsStore } from '@/stores/statistics';
import { storeToRefs } from 'pinia';
import * as echarts from 'echarts';

const statisticsStore = useStatisticsStore();
const { statistics, loading } = storeToRefs(statisticsStore);

const chartType = ref('pie');
const pieChartRef = ref(null);
const barChartRef = ref(null);
const lineChartRef = ref(null);
let pieChart = null;
let barChart = null;
let lineChart = null;

onMounted(async () => {
  await statisticsStore.fetchStatistics();
  await nextTick();
  setTimeout(() => {
  initCharts();
  }, 100);
});

watch(statistics, () => {
  if (statistics.value) {
    nextTick(() => {
    updateCharts();
    });
  }
}, { deep: true });

const refreshData = async () => {
  await statisticsStore.fetchStatistics();
};

const handleChartTypeChange = () => {
  nextTick(() => {
    setTimeout(() => {
    if (chartType.value === 'bar' && barChartRef.value) {
      if (barChart) barChart.dispose();
      barChart = echarts.init(barChartRef.value);
      updateBarChart();
    }
    if (chartType.value === 'pie' && pieChartRef.value) {
      if (pieChart) pieChart.dispose();
      pieChart = echarts.init(pieChartRef.value);
      updatePieChart();
    }
    if (chartType.value === 'line' && lineChartRef.value) {
      if (lineChart) lineChart.dispose();
      lineChart = echarts.init(lineChartRef.value);
      updateLineChart();
    }
    }, 50);
  });
};

const initCharts = () => {
  if (pieChartRef.value && pieChartRef.value.clientWidth > 0) {
    pieChart = echarts.init(pieChartRef.value);
  }
  if (barChartRef.value && barChartRef.value.clientWidth > 0) {
    barChart = echarts.init(barChartRef.value);
  }
  if (lineChartRef.value && lineChartRef.value.clientWidth > 0) {
    lineChart = echarts.init(lineChartRef.value);
  }
  updateCharts();
};

const updateCharts = () => {
  if (chartType.value === 'pie') updatePieChart();
  else if (chartType.value === 'bar') updateBarChart();
  else if (chartType.value === 'line') updateLineChart();
};

const updatePieChart = async () => {
  if (!pieChart || !statistics.value) return;
  try {
    const categoryData = await statisticsStore.fetchCategoryStatistics();
    const pieData = categoryData.categoryStats.map(stat => ({
      name: stat.categoryName,
      value: stat.amount
    }));
    const option = {
      tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', left: 'left' },
      series: [
        {
          name: '类别统计',
          type: 'pie',
          radius: '50%',
          data: pieData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };
    pieChart.setOption(option);
  } catch (error) {
    console.error('更新饼图失败:', error);
  }
};

const updateBarChart = async () => {
  if (!barChart || !statistics.value) return;
  try {
    const monthlyData = await statisticsStore.fetchMonthlyStatistics();
    const months = monthlyData.monthlyStats.map(stat => stat.month);
    const incomeData = monthlyData.monthlyStats.map(stat => stat.income);
    const expenseData = monthlyData.monthlyStats.map(stat => stat.expense);
    const option = {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: ['收入', '支出'] },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value' },
      series: [
        { name: '收入', type: 'bar', data: incomeData, itemStyle: { color: '#67c23a' } },
        { name: '支出', type: 'bar', data: expenseData, itemStyle: { color: '#f56c6c' } }
      ]
    };
    barChart.setOption(option);
  } catch (error) {
    console.error('更新柱状图失败:', error);
  }
};

const updateLineChart = async () => {
  if (!lineChart || !statistics.value) return;
  try {
    const dailyData = await statisticsStore.fetchDailyStatistics();
    const dates = dailyData.dailyStats.map(stat => stat.date);
    const incomeData = dailyData.dailyStats.map(stat => stat.income);
    const expenseData = dailyData.dailyStats.map(stat => stat.expense);
    const option = {
      tooltip: { trigger: 'axis' },
      legend: { data: ['收入', '支出'] },
      xAxis: { type: 'category', data: dates.slice(-30) },
      yAxis: { type: 'value' },
      series: [
        { name: '收入', type: 'line', data: incomeData.slice(-30), smooth: true, itemStyle: { color: '#67c23a' } },
        { name: '支出', type: 'line', data: expenseData.slice(-30), smooth: true, itemStyle: { color: '#f56c6c' } }
      ]
    };
    lineChart.setOption(option);
  } catch (error) {
    console.error('更新折线图失败:', error);
  }
};

const formatAmount = (amount) => {
  return Number(amount).toFixed(2);
};

const getCategoryPercent = (amount, type) => {
  if (!statistics.value) return 0;
  const total = type === 'income' ? Number(statistics.value.totalIncome) : Number(statistics.value.totalExpense);
  if (!total) return 0;
  return Math.round((Number(amount) / total) * 100);
};

// 监听窗口大小变化，调整图表大小
window.addEventListener('resize', () => {
  if (pieChart) pieChart.resize();
  if (barChart) barChart.resize();
  if (lineChart) lineChart.resize();
});
</script>

<style scoped>
.statistics-view {
  padding: 24px;
  background: #f6f8fa;
  min-height: 100vh;
}
.statistics-header {
  margin-bottom: 24px;
}
.stat-card {
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.06);
  text-align: center;
  padding: 24px 0;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0e7ff 100%);
}
.net-card .stat-value.net {
  font-size: 2.2rem;
  color: #409eff;
  font-weight: bold;
}
.income-card .stat-value.income {
  font-size: 1.8rem;
  color: #67c23a;
  font-weight: bold;
}
.expense-card .stat-value.expense {
  font-size: 1.8rem;
  color: #f56c6c;
  font-weight: bold;
}
.statistics-main {
  margin-top: 0;
}
.info-card {
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.06);
  margin-bottom: 24px;
}
.info-item {
  margin-bottom: 18px;
  font-size: 15px;
}
.top-categories {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 6px;
}
.category-tag {
  background-color: #f0f0f0;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.category-tag.income {
  background-color: #eaffea;
}
.cat-amount {
  color: #888;
  margin-left: 2px;
}
.chart-card {
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.06);
  min-height: 420px;
}
.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.chart-container {
  height: 400px;
}
.chart {
  width: 100%;
  height: 100%;
}
.pie-chart-container,
.bar-chart-container,
.line-chart-container {
  height: 100%;
}
</style> 