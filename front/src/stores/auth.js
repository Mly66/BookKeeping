import { defineStore } from 'pinia';
import apiClient from '@/api';
import router from '@/router';
import { ElMessage } from 'element-plus';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null,
        token: null,
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
    },
    actions: {
        async login(credentials) {
            try {
                const response = await apiClient.post('/auth/login', credentials);
                this.token = response.data.token;
                await this.fetchProfile();
                router.push('/');
            } catch (error) {
                console.error('Login failed:', error);
                // Error is already handled by the interceptor
            }
        },
        async register(userInfo) {
            try {
                await apiClient.post('/auth/register', userInfo);
                ElMessage.success('注册成功！');
                router.push('/login');
            } catch (error) {
                console.error('Registration failed:', error);
            }
        },
        async fetchProfile() {
            if (this.token) {
                try {
                    const response = await apiClient.get('/user/profile');
                    this.user = response.data;
                } catch (error) {
                    console.error('Failed to fetch profile:', error);
                    this.clearAuth();
                }
            }
        },
        async updateUserProfile(profileData) {
            try {
                const response = await apiClient.put('/user/profile', profileData);
                this.user = response.data;
                ElMessage.success('个人信息更新成功！');
            } catch (error) {
                console.error('Profile update failed:', error);
            }
        },
        async changePassword(passwordData) {
            try {
                await apiClient.post('/user/change-password', passwordData);
                ElMessage.success('密码修改成功，请重新登录！');
                this.clearAuth();
            } catch (error) {
                console.error('Password change failed:', error);
                // Re-throw to be caught in the component for form handling
                throw error;
            }
        },
        logout() {
            this.clearAuth();
        },
        clearAuth() {
            this.user = null;
            this.token = null;
            router.push('/login');
        },
        clearExpiredToken() {
            this.user = null;
            this.token = null;
            ElMessage({
                message: '检测到过期令牌，请重新登录',
                type: 'warning',
            });
            router.push('/login');
        },
    },
    persist: {
        paths: ['token'],
    },
}); 