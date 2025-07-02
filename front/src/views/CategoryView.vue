<template>
  <div class="category-view">
    <el-card class="category-card">
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <el-button type="primary" @click="openDialog()" class="add-btn">新增分类</el-button>
        </div>
      </template>
      <el-table :data="categories" style="width: 100%" class="category-table">
        <el-table-column prop="name" label="名称">
          <template #default="{ row }">
            <span class="cat-icon" :class="row.type">{{ row.name.slice(0,1) }}</span>
            <span class="cat-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型">
            <template #default="{ row }">
                <el-tag :type="row.type === 'expense' ? 'danger' : 'success'">
                    {{ row.type === 'expense' ? '支出' : '收入' }}
                </el-tag>
            </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除这个分类吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500" class="cat-dialog">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="支出" value="expense" />
            <el-option label="收入" value="income" />
          </el-select>
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
import { ref, onMounted, computed, reactive } from 'vue';
import { useCategoryStore } from '@/stores/category';
import { storeToRefs } from 'pinia';

const categoryStore = useCategoryStore();
const { categories } = storeToRefs(categoryStore);

const dialogVisible = ref(false);
const isEdit = ref(false);
const form = reactive({
  id: null,
  name: '',
  type: 'expense',
});

onMounted(() => {
  categoryStore.fetchCategories();
});

const openDialog = (category = null) => {
  if (category) {
    isEdit.value = true;
    form.id = category.id;
    form.name = category.name;
    form.type = category.type;
  } else {
    isEdit.value = false;
    form.id = null;
    form.name = '';
    form.type = 'expense';
  }
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  if (isEdit.value) {
    await categoryStore.updateCategory(form.id, { name: form.name, type: form.type });
  } else {
    await categoryStore.createCategory({ name: form.name, type: form.type });
  }
  dialogVisible.value = false;
};

const handleDelete = async (id) => {
  await categoryStore.deleteCategory(id);
};
</script>

<style scoped>
.category-view {
  padding: 24px;
  background: #f6f8fa;
  min-height: 100vh;
}
.category-card {
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.06);
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
.category-table {
  border-radius: 12px;
  overflow: hidden;
}
.cat-icon {
  display: inline-block;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  text-align: center;
  line-height: 28px;
  font-weight: bold;
  margin-right: 8px;
  color: #fff;
  background: #409eff;
}
.cat-icon.expense {
  background: #f56c6c;
}
.cat-icon.income {
  background: #67c23a;
}
.cat-name {
  font-weight: 500;
}
.cat-dialog .el-dialog {
  border-radius: 16px;
}
</style> 