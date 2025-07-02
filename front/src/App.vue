<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

onMounted(async () => {
  // 如果存在令牌，尝试获取用户信息来验证令牌有效性
  if (authStore.token) {
    try {
      await authStore.fetchProfile();
    } catch (error) {
      // 如果获取用户信息失败，说明令牌已过期
      console.log('Token validation failed, clearing expired token');
      authStore.clearExpiredToken();
    }
  }
});
</script>

<style scoped>
</style>
