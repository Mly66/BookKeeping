import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import { ElMessage } from 'element-plus';

const apiClient = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// 用于存储正在进行的刷新请求
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    
    failedQueue = [];
};

apiClient.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore();
        const token = authStore.token;
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

apiClient.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        const originalRequest = error.config;
        const authStore = useAuthStore();

        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            // 如果是刷新令牌的请求失败，直接清除认证状态
            if (originalRequest.url === '/auth/refresh') {
                authStore.clearAuth();
                ElMessage({
                    message: '登录已过期，请重新登录',
                    type: 'warning',
                });
                return Promise.reject(error);
            }

            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then(token => {
                    originalRequest.headers.Authorization = `Bearer ${token}`;
                    return apiClient(originalRequest);
                }).catch(err => {
                    return Promise.reject(err);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                // 尝试刷新令牌
                const response = await apiClient.post('/auth/refresh', {}, {
                    headers: {
                        Authorization: `Bearer ${authStore.token}`
                    }
                });

                const newToken = response.data.token;
                authStore.token = newToken;
                
                // 处理队列中的请求
                processQueue(null, newToken);
                
                // 重试原始请求
                originalRequest.headers.Authorization = `Bearer ${newToken}`;
                return apiClient(originalRequest);
            } catch (refreshError) {
                // 刷新失败，处理队列中的请求
                processQueue(refreshError, null);
                
                // 清除认证状态并跳转到登录页
                authStore.clearAuth();
                
                ElMessage({
                    message: '登录已过期，请重新登录',
                    type: 'warning',
                });
                
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        // 其他错误处理
        if (error.response) {
            // 不显示401错误的消息，因为已经处理了
            if (error.response.status !== 401) {
                ElMessage({
                    message: error.response.data.message || '请求出错',
                    type: 'error',
                });
            }
        } else {
            ElMessage({
                message: '网络错误或服务器无响应',
                type: 'error',
            });
        }
        
        return Promise.reject(error);
    }
);

export default apiClient; 