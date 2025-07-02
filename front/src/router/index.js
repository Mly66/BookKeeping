import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

import MainLayout from '@/layouts/MainLayout.vue';
import LoginView from '@/views/LoginView.vue';
import RegisterView from '@/views/RegisterView.vue';
import DashboardView from '@/views/DashboardView.vue';
import StatsView from '@/views/StatsView.vue';
import ProfileView from '@/views/ProfileView.vue';
import CategoryView from '@/views/CategoryView.vue';
import AiChatView from '@/views/AiChatView.vue';

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: LoginView,
    },
    {
        path: '/register',
        name: 'Register',
        component: RegisterView,
    },
    {
        path: '/',
        component: MainLayout,
        meta: { requiresAuth: true },
        children: [
            {
                path: '',
                name: 'Dashboard',
                component: DashboardView,
            },
            {
                path: 'categories',
                name: 'Categories',
                component: CategoryView,
            },
            {
                path: 'stats',
                name: 'Stats',
                component: StatsView,
            },
            {
                path: 'profile',
                name: 'Profile',
                component: ProfileView,
            },
            {
                path: 'ai-chat',
                name: 'AiChat',
                component: AiChatView,
            },
        ],
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();
    const isAuthenticated = authStore.isAuthenticated;

    if (to.meta.requiresAuth && !isAuthenticated) {
        next({ name: 'Login' });
    } else {
        next();
    }
});

export default router; 