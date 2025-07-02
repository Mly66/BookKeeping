<template>
  <el-container class="main-layout">
    <el-aside width="200px">
      <el-menu :default-active="$route.path" router class="el-menu-vertical-demo">
        <div class="logo-container">
          <h3>记账本</h3>
        </div>
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Tickets /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/stats">
          <el-icon><DataLine /></el-icon>
          <span>统计</span>
        </el-menu-item>
        <el-menu-item index="/ai-chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI助手</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="main-header">
        <div></div>
        <div class="user-info">
          <span>你好, {{ authStore.user?.nickname || authStore.user?.username }}</span>
          <el-button type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';
import { House, DataLine, User, Tickets, ChatDotRound } from '@element-plus/icons-vue';

const authStore = useAuthStore();

const handleLogout = () => {
  authStore.logout();
};
</script>

<style scoped>
.main-layout, .el-menu {
  height: 100%;
}
.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #dcdfe6;
}
.logo-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60px;
  background-color: #ffffff;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}
</style> 