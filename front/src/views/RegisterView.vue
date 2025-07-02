<template>
  <div class="register-bg">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>
    
    <div class="register-container">
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
              <span>快速注册，立即体验</span>
            </div>
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>免费使用，无隐藏费用</span>
            </div>
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>数据安全，隐私保护</span>
            </div>
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>7×24小时技术支持</span>
            </div>
          </div>
          <div class="testimonials">
            <div class="testimonial-item">
              <div class="testimonial-content">
                "Bookkeep让我的财务管理变得简单高效，强烈推荐！"
              </div>
              <div class="testimonial-author">
                <div class="author-avatar">张</div>
                <div class="author-info">
                  <div class="author-name">张先生</div>
                  <div class="author-title">企业财务总监</div>
                </div>
              </div>
            </div>
          </div>
          <div class="security-badges">
            <div class="badge">
              <el-icon><Lock /></el-icon>
              <span>SSL加密</span>
            </div>
            <div class="badge">
              <el-icon><Lock /></el-icon>
              <span>数据加密</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧注册表单区 -->
      <div class="form-section">
        <el-card class="register-card">
          <template #header>
            <div class="card-header">
              <h2 class="welcome-text">创建账户</h2>
              <p class="welcome-subtitle">加入我们，开始您的智能记账之旅</p>
            </div>
          </template>
          
          <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-width="0" class="register-form">
            <el-form-item prop="username">
              <el-input 
                v-model="registerForm.username" 
                placeholder="请输入用户名（3-20个字符）"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="email">
              <el-input 
                v-model="registerForm.email" 
                placeholder="请输入邮箱地址"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                type="password" 
                v-model="registerForm.password" 
                show-password 
                placeholder="请输入密码（至少6位）"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input 
                type="password" 
                v-model="registerForm.confirmPassword" 
                show-password 
                placeholder="请再次输入密码"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <div class="password-strength" v-if="registerForm.password">
              <div class="strength-label">密码强度：</div>
              <div class="strength-bar">
                <div class="strength-fill" :class="passwordStrengthClass"></div>
              </div>
              <div class="strength-text" :class="passwordStrengthClass">{{ passwordStrengthText }}</div>
            </div>
            
            <div class="form-options">
              <el-checkbox v-model="agreeTerms">
                我已阅读并同意
                <a href="#" class="terms-link">《用户协议》</a>
                和
                <a href="#" class="terms-link">《隐私政策》</a>
              </el-checkbox>
            </div>
            
            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleRegister" 
                :loading="loading" 
                class="register-btn"
                size="large"
                :disabled="!agreeTerms"
              >
                <el-icon v-if="!loading"><User /></el-icon>
                {{ loading ? '注册中...' : '立即注册' }}
              </el-button>
            </el-form-item>
            
            <div class="divider">
              <span>或</span>
            </div>
            
            <div class="social-login">
              <el-button class="social-btn wechat">
                <el-icon><Message /></el-icon>
                微信注册
              </el-button>
              <el-button class="social-btn qq">
                <el-icon><Message /></el-icon>
                QQ注册
              </el-button>
            </div>
            
            <div class="login-link">
              <span>已有账户？</span>
              <router-link to="/login" class="link-btn">立即登录</router-link>
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
import { ref, reactive, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { User, Lock, Message, Files, CircleCheck } from '@element-plus/icons-vue';

const authStore = useAuthStore();
const registerFormRef = ref(null);
const loading = ref(false);
const agreeTerms = ref(false);

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
});

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validatePass, trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' }, 
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
});

// 密码强度检测
const passwordStrength = computed(() => {
  const password = registerForm.password;
  if (!password) return 0;
  
  let score = 0;
  if (password.length >= 6) score += 1;
  if (password.length >= 8) score += 1;
  if (/[a-z]/.test(password)) score += 1;
  if (/[A-Z]/.test(password)) score += 1;
  if (/[0-9]/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;
  
  return Math.min(score, 5);
});

const passwordStrengthClass = computed(() => {
  const strength = passwordStrength.value;
  if (strength <= 1) return 'weak';
  if (strength <= 3) return 'medium';
  return 'strong';
});

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value;
  if (strength <= 1) return '弱';
  if (strength <= 3) return '中等';
  return '强';
});

const handleRegister = async () => {
  if (!registerFormRef.value) return;
  await registerFormRef.value.validate(async (valid) => {
    if (valid && agreeTerms.value) {
      loading.value = true;
      try {
        await authStore.register({
          username: registerForm.username.trim(),
          password: registerForm.password.trim(),
          email: registerForm.email.trim(),
        });
      } catch (error) {
        console.error('注册失败:', error);
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.register-bg {
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

.register-container {
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

.testimonials {
  margin: 40px 0;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.testimonial-content {
  font-style: italic;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 15px;
  opacity: 0.9;
}

.testimonial-author {
  display: flex;
  align-items: center;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #67c23a;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  margin-right: 12px;
}

.author-name {
  font-weight: 600;
  font-size: 14px;
}

.author-title {
  font-size: 12px;
  opacity: 0.8;
}

.security-badges {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.badge {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.badge .el-icon {
  margin-right: 6px;
  color: #67c23a;
}

.form-section {
  flex: 1;
  max-width: 500px;
  z-index: 1;
}

.register-card {
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

.register-form {
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

.password-strength {
  display: flex;
  align-items: center;
  margin: 15px 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.strength-label {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
  white-space: nowrap;
}

.strength-bar {
  flex: 1;
  height: 6px;
  background: #e4e7ed;
  border-radius: 3px;
  margin-right: 10px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 3px;
}

.strength-fill.weak {
  width: 33.33%;
  background: #f56c6c;
}

.strength-fill.medium {
  width: 66.66%;
  background: #e6a23c;
}

.strength-fill.strong {
  width: 100%;
  background: #67c23a;
}

.strength-text {
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.strength-text.weak {
  color: #f56c6c;
}

.strength-text.medium {
  color: #e6a23c;
}

.strength-text.strong {
  color: #67c23a;
}

.form-options {
  margin: 20px 0;
}

.terms-link {
  color: #409eff;
  text-decoration: none;
  transition: color 0.3s ease;
}

.terms-link:hover {
  color: #67c23a;
}

.register-btn {
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

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(64, 158, 255, 0.4);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.register-btn .el-icon {
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

.login-link {
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
  .register-container {
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
  
  .security-badges {
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .register-container {
    padding: 10px;
  }
  
  .form-section {
    max-width: 100%;
  }
  
  .register-card {
    border-radius: 16px;
  }
  
  .register-form {
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
  
  .testimonials {
    padding: 15px;
  }
}
</style> 