<template>
  <div class="dashboard-view">
    <el-row :gutter="20" class="dashboard-header">
      <el-col :span="8">
        <el-card class="stat-card net-card">
          <div class="stat-title">净资产</div>
          <div class="stat-value net">￥{{ netAsset }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card income-card">
          <div class="stat-title">本月收入</div>
          <div class="stat-value income">+￥{{ monthIncome }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card expense-card">
          <div class="stat-title">本月支出</div>
          <div class="stat-value expense">-￥{{ monthExpense }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-main">
      <el-col :span="16">
        <el-card class="bills-card">
          <template #header>
            <div class="card-header">
              <span>我的账单</span>
              <el-button type="primary" @click="openDialog()" class="add-btn">新增账单</el-button>
            </div>
          </template>
          <el-table :data="bills" style="width: 100%" class="bills-table">
            <el-table-column prop="categoryName" label="分类" />
            <el-table-column prop="amount" label="金额">
              <template #default="{ row }">
                <span :class="row.type === 'expense' ? 'expense-color' : 'income-color'">
                  {{ row.type === 'expense' ? '-' : '+' }} {{ row.amount.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="remarks" label="备注" />
            <el-table-column prop="billTime" label="账单时间" />
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button size="small" @click="openDialog(row)">编辑</el-button>
                <el-popconfirm title="确定删除这笔账单吗？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button size="small" type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="recent-bill-card">
          <template #header>最近一笔账单</template>
          <div v-if="bills.length" class="recent-bill-info">
            <div class="recent-category">
              <span class="recent-type" :class="bills[0].type">{{ bills[0].type === 'expense' ? '支出' : '收入' }}</span>
              <span class="recent-category-name">{{ bills[0].categoryName }}</span>
            </div>
            <div class="recent-amount" :class="bills[0].type">{{ bills[0].type === 'expense' ? '-' : '+' }}{{ bills[0].amount.toFixed(2) }}</div>
            <div class="recent-time">{{ bills[0].billTime }}</div>
            <div class="recent-remarks">{{ bills[0].remarks }}</div>
          </div>
          <div v-else class="recent-empty">暂无账单</div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑账单' : '新增账单'" width="500">
      <el-form :model="form" label-width="80px">
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio-button value="expense">支出</el-radio-button>
            <el-radio-button value="income">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option v-for="cat in filteredCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :precision="2" :step="1" :min="0" />
        </el-form-item>
        <el-form-item label="账单时间">
          <el-date-picker v-model="form.billTime" type="datetime" placeholder="选择日期时间" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remarks" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { useBillStore } from '@/stores/bill';
import { useCategoryStore } from '@/stores/category';
import { storeToRefs } from 'pinia';

const billStore = useBillStore();
const { bills } = storeToRefs(billStore);

const categoryStore = useCategoryStore();
const { categories } = storeToRefs(categoryStore);

const dialogVisible = ref(false);
const isEdit = ref(false);
const form = reactive({
  id: null,
  categoryId: null,
  amount: 0,
  type: 'expense',
  remarks: '',
  billTime: new Date().toISOString().slice(0, 19),
});

const filteredCategories = computed(() => {
    return categories.value.filter(cat => cat.type === form.type);
});

onMounted(() => {
  billStore.fetchBills();
  categoryStore.fetchCategories();
});

const openDialog = (bill = null) => {
  if (bill) {
    isEdit.value = true;
    form.id = bill.id;
    form.categoryId = bill.categoryId;
    form.amount = bill.amount;
    form.type = bill.type;
    form.remarks = bill.remarks;
    form.billTime = bill.billTime;
  } else {
    isEdit.value = false;
    form.id = null;
    form.categoryId = null;
    form.amount = 0;
    form.type = 'expense';
    form.remarks = '';
    form.billTime = new Date().toISOString().slice(0, 19);
  }
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  if(isEdit.value) {
    const dataToSubmit = { ...form };
    await billStore.updateBill(dataToSubmit.id, dataToSubmit);
  } else {
    const { id, ...dataToSubmit } = form;
    await billStore.createBill(dataToSubmit);
  }
  dialogVisible.value = false;
};

const handleDelete = async (id) => {
  await billStore.deleteBill(id);
};

// 统计数据
const netAsset = computed(() => {
  let income = 0, expense = 0;
  bills.value.forEach(b => {
    if (b.type === 'income') income += b.amount;
    else expense += b.amount;
  });
  return (income - expense).toFixed(2);
});
const monthIncome = computed(() => {
  const now = new Date();
  const ym = now.toISOString().slice(0,7);
  return bills.value.filter(b => b.type === 'income' && b.billTime.startsWith(ym)).reduce((sum, b) => sum + b.amount, 0).toFixed(2);
});
const monthExpense = computed(() => {
  const now = new Date();
  const ym = now.toISOString().slice(0,7);
  return bills.value.filter(b => b.type === 'expense' && b.billTime.startsWith(ym)).reduce((sum, b) => sum + b.amount, 0).toFixed(2);
});
</script>

<style scoped>
.dashboard-view {
  padding: 24px;
  background: #f6f8fa;
  min-height: 100vh;
}
.dashboard-header {
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
.dashboard-main {
  margin-top: 0;
}
.bills-card {
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.06);
  margin-bottom: 24px;
}
.bills-table {
  border-radius: 12px;
  overflow: hidden;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.add-btn {
  background: linear-gradient(90deg, #409eff 0%, #67c23a 100%);
  border: none;
  color: #fff;
  font-weight: bold;
  border-radius: 20px;
  box-shadow: 0 2px 8px 0 rgba(64,158,255,0.12);
}
.add-btn:hover {
  filter: brightness(1.1);
}
.recent-bill-card {
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.06);
  min-height: 220px;
}
.recent-bill-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 0;
}
.recent-category {
  font-size: 1.1rem;
  font-weight: 500;
}
.recent-type {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 0.95rem;
  margin-right: 8px;
  background: #f0f9ff;
  color: #409eff;
}
.recent-type.expense {
  background: #fff0f0;
  color: #f56c6c;
}
.recent-type.income {
  background: #f0fff0;
  color: #67c23a;
}
.recent-category-name {
  font-weight: 600;
}
.recent-amount {
  font-size: 1.5rem;
  font-weight: bold;
}
.recent-amount.expense {
  color: #f56c6c;
}
.recent-amount.income {
  color: #67c23a;
}
.recent-time {
  font-size: 0.95rem;
  color: #888;
}
.recent-remarks {
  font-size: 0.95rem;
  color: #666;
}
.recent-empty {
  color: #bbb;
  text-align: center;
  padding: 32px 0;
}
.expense-color {
  color: #f56c6c;
}
.income-color {
  color: #67c23a;
}
</style> 