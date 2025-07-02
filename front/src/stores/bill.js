import { defineStore } from 'pinia';
import apiClient from '@/api';
import { ElMessage } from 'element-plus';

export const useBillStore = defineStore('bill', {
    state: () => ({
        bills: [],
    }),
    actions: {
        async fetchBills() {
            try {
                const response = await apiClient.get('/bills');
                this.bills = response.data;
            } catch (error) {
                console.error('Failed to fetch bills:', error);
            }
        },
        async createBill(billData) {
            try {
                await apiClient.post('/bills', billData);
                ElMessage.success('账单创建成功！');
                await this.fetchBills();
            } catch (error) {
                console.error('Failed to create bill:', error);
            }
        },
        async updateBill(id, billData) {
            try {
                await apiClient.put(`/bills/${id}`, billData);
                ElMessage.success('账单更新成功！');
                await this.fetchBills();
            } catch (error) {
                console.error('Failed to update bill:', error);
            }
        },
        async deleteBill(id) {
            try {
                await apiClient.delete(`/bills/${id}`);
                ElMessage.success('账单删除成功！');
                await this.fetchBills();
            } catch (error) {
                console.error('Failed to delete bill:', error);
            }
        },
    },
}); 