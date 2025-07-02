import { defineStore } from 'pinia';
import apiClient from '@/api';
import { ElMessage } from 'element-plus';

export const useCategoryStore = defineStore('category', {
    state: () => ({
        categories: [],
    }),
    actions: {
        async fetchCategories() {
            try {
                const response = await apiClient.get('/categories');
                this.categories = response.data;
            } catch (error) {
                console.error('Failed to fetch categories:', error);
            }
        },
        async createCategory(categoryData) {
            try {
                await apiClient.post('/categories', categoryData);
                ElMessage.success('分类创建成功！');
                await this.fetchCategories(); // Refresh list
            } catch (error) {
                console.error('Failed to create category:', error);
            }
        },
        async updateCategory(id, categoryData) {
            try {
                await apiClient.put(`/categories/${id}`, categoryData);
                ElMessage.success('分类更新成功！');
                await this.fetchCategories(); // Refresh list
            } catch (error) {
                console.error('Failed to update category:', error);
            }
        },
        async deleteCategory(id) {
            try {
                await apiClient.delete(`/categories/${id}`);
                ElMessage.success('分类删除成功！');
                await this.fetchCategories(); // Refresh list
            } catch (error) {
                console.error('Failed to delete category:', error);
            }
        },
    },
}); 