<template>
  <div class="login-bg">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>
    
    <div class="login-container">
      <!-- 左侧品牌展示区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-container">
            <div class="logo-icon">
              <el-icon size="48"><Files /></el-icon>
            </div>
            <h1 class="brand-title">Bookkeep</h1>
            <p class="brand-subtitle">智能记账管理系统</p>
          </div>
          <div class="features-list">
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>智能分类记账</span>
            </div>
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>数据可视化分析</span>
            </div>
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>多端同步备份</span>
            </div>
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>安全隐私保护</span>
            </div>
          </div>
          <div class="stats-preview">
            <div class="stat-item">
              <div class="stat-number">10K+</div>
              <div class="stat-label">活跃用户</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">99.9%</div>
              <div class="stat-label">系统稳定性</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="form-section">
        <el-card class="login-card">
          <template #header>
            <div class="card-header">
              <h2 class="welcome-text">欢迎回来</h2>
              <p class="welcome-subtitle">请登录您的账户继续使用</p>
            </div>
          </template>
          
          <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0" class="login-form">
            <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                type="password" 
                v-model="loginForm.password" 
                show-password 
                placeholder="请输入密码"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <a href="#" class="forgot-password">忘记密码？</a>
            </div>
            
            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleLogin" 
                :loading="loading" 
                class="login-btn"
                size="large"
              >
                <el-icon v-if="!loading"><User /></el-icon>
                {{ loading ? '登录中...' : '立即登录' }}
              </el-button>
            </el-form-item>
            
            <div class="divider">
              <span>或</span>
            </div>
            
            <div class="social-login">
              <el-button class="social-btn wechat">
                <el-icon><Message /></el-icon>
                微信登录
              </el-button>
              <el-button class="social-btn qq">
                <el-icon><Message /></el-icon>
                QQ登录
              </el-button>
            </div>
            
            <div class="register-link">
              <span>还没有账户？</span>
              <router-link to="/register" class="link-btn">立即注册</router-link>
            </div>
          </el-form>
        </el-card>
      </div>
    </div>
    
    <div class="copyright">
      <p>© 2025 Bookkeep | 智能记账管理系统</p>
      <p class="copyright-links">
        <a href="#">隐私政策</a> | 
        <a href="#">服务条款</a> | 
        <a href="#">帮助中心</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { User, Lock, Files, CircleCheck, Message } from '@element-plus/icons-vue';

const authStore = useAuthStore();
const loginFormRef = ref(null);
const loading = ref(false);
const rememberMe = ref(false);

const loginForm = reactive({
  username: '',
  password: '',
});

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
});

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await authStore.login({
          username: loginForm.username.trim(),
          password: loginForm.password.trim()
        });
      } catch (error) {
        console.error('登录失败:', error);
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 80px;
  height: 80px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 120px;
  height: 120px;
  top: 20%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 60px;
  height: 60px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

.shape-4 {
  width: 100px;
  height: 100px;
  bottom: 10%;
  right: 10%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  gap: 60px;
}

.brand-section {
  flex: 1;
  max-width: 500px;
  color: white;
  z-index: 1;
}

.brand-content {
  padding: 40px;
}

.logo-container {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 10px;
  background: linear-gradient(45deg, #fff, #f0f9ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
}

.features-list {
  margin: 40px 0;
}

.feature-item {
  display: flex;
  align-items: center;
  margin: 15px 0;
  font-size: 16px;
  opacity: 0.9;
}

.feature-item .el-icon {
  margin-right: 12px;
  color: #67c23a;
  font-size: 18px;
}

.stats-preview {
  display: flex;
  justify-content: space-around;
  margin-top: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #67c23a;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 5px;
}

.form-section {
  flex: 1;
  max-width: 450px;
  z-index: 1;
}

.login-card {
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.card-header {
  text-align: center;
  padding: 20px 0;
}

.welcome-text {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px;
}

.welcome-subtitle {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0;
  font-weight: 400;
}

.login-form {
  padding: 0 30px 30px;
}

.custom-input {
  border-radius: 12px;
  overflow: hidden;
}

.custom-input :deep(.el-input__wrapper) {
  background: #f8f9fa;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: none;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #409eff;
  background: #fff;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.input-icon {
  color: #409eff;
  font-size: 18px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
}

.forgot-password {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #67c23a;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border: none;
  color: #fff;
  font-weight: 600;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
  font-size: 16px;
  letter-spacing: 1px;
  transition: all 0.3s ease;
  height: 48px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(64, 158, 255, 0.4);
}

.login-btn .el-icon {
  margin-right: 8px;
}

.divider {
  text-align: center;
  margin: 30px 0;
  position: relative;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e4e7ed;
}

.divider span {
  background: #fff;
  padding: 0 20px;
  color: #909399;
  font-size: 14px;
}

.social-login {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
}

.social-btn {
  flex: 1;
  border-radius: 12px;
  border: 2px solid #e4e7ed;
  background: #fff;
  color: #606266;
  font-weight: 500;
  transition: all 0.3s ease;
  height: 44px;
}

.social-btn:hover {
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-1px);
}

.social-btn.wechat:hover {
  border-color: #07c160;
  color: #07c160;
}

.social-btn.qq:hover {
  border-color: #12b7f5;
  color: #12b7f5;
}

.social-btn .el-icon {
  margin-right: 8px;
}

.register-link {
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.link-btn {
  color: #409eff;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.link-btn:hover {
  color: #67c23a;
}

.copyright {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  z-index: 1;
}

.copyright p {
  margin: 5px 0;
}

.copyright-links a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  margin: 0 10px;
  transition: color 0.3s ease;
}

.copyright-links a:hover {
  color: #fff;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-container {
    flex-direction: column;
    gap: 40px;
  }
  
  .brand-section {
    max-width: 100%;
    text-align: center;
  }
  
  .brand-content {
    padding: 20px;
  }
  
  .brand-title {
    font-size: 36px;
  }
  
  .stats-preview {
    justify-content: center;
    gap: 40px;
  }
}

@media (max-width: 768px) {
  .login-container {
    padding: 10px;
  }
  
  .form-section {
    max-width: 100%;
  }
  
  .login-card {
    border-radius: 16px;
  }
  
  .login-form {
    padding: 0 20px 20px;
  }
  
  .social-login {
    flex-direction: column;
  }
  
  .brand-title {
    font-size: 28px;
  }
  
  .welcome-text {
    font-size: 24px;
  }
}
</style> 