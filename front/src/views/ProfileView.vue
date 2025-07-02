<template>
  <div class="profile-view">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="user-card">
          <template #header>
            <div class="user-header">个人信息</div>
          </template>
          <div class="user-avatar">
            <el-avatar :size="80" :src="user?.avatar || 'https://free-img.400040.xyz/4/2025/06/18/6671a610741eb.png'" />
          </div>
          <div class="user-info">
            <div class="user-nickname">{{ user?.nickname || user?.username || '未设置昵称' }}</div>
            <div class="user-email"><el-icon><Message /></el-icon> {{ user?.email || '未绑定邮箱' }}</div>
            <div class="user-phone"><el-icon><Phone /></el-icon> {{ user?.phone || '未绑定手机' }}</div>
            <div class="user-meta">注册时间：{{
              user?.createdAt
                ? user.createdAt.slice(0, 10)
                : user?.createTime
                ? user.createTime.slice(0, 10)
                : user?.regTime
                ? user.regTime.slice(0, 10)
                : '-'
            }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="profile-card">
          <template #header>
            <div>修改资料</div>
          </template>
          <el-form v-if="user" :model="userForm" label-width="80px">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="userForm.nickname" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" />
            </el-form-item>
            <el-form-item label="手机" prop="phone">
              <el-input v-model="userForm.phone" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" class="profile-btn">更新信息</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="profile-card">
          <template #header>
            <div>修改密码</div>
          </template>
           <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input type="password" v-model="passwordForm.oldPassword" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input type="password" v-model="passwordForm.newPassword" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input type="password" v-model="passwordForm.confirmPassword" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" class="profile-btn">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { storeToRefs } from 'pinia';
import { UserFilled, Message, Phone } from '@element-plus/icons-vue';

const authStore = useAuthStore();
const { user } = storeToRefs(authStore);

const userForm = reactive({
  nickname: '',
  email: '',
  phone: '',
});

const passwordFormRef = ref(null);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const passwordRules = reactive({
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
});

watch(user, (newUser) => {
  if (newUser) {
    userForm.nickname = newUser.nickname || '';
    userForm.email = newUser.email || '';
    userForm.phone = newUser.phone || '';
  }
}, { immediate: true });

onMounted(() => {
  if (!user.value) {
    authStore.fetchProfile();
  }
});

const handleUpdateProfile = () => {
  authStore.updateUserProfile(userForm);
};

const handleChangePassword = async () => {
    if (!passwordFormRef.value) return;
    await passwordFormRef.value.validate(async (valid) => {
        if(valid) {
            try {
                await authStore.changePassword({
                    oldPassword: passwordForm.oldPassword,
                    newPassword: passwordForm.newPassword,
                });
                passwordFormRef.value.resetFields();
            } catch (error) {
                // Error message is already shown by API interceptor
                console.log("密码修改失败，请检查旧密码是否正确。")
            }
        }
    });
};
</script>

<style scoped>
.profile-view {
  padding: 24px;
  background: #f6f8fa;
  min-height: 100vh;
}
.user-card {
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.06);
  text-align: center;
  padding-bottom: 16px;
}
.user-header {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  text-align: center;
}
.user-avatar {
  margin: 24px 0 12px 0;
  display: flex;
  justify-content: center;
}
.user-info {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}
.user-nickname {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 6px;
}
.user-email, .user-phone {
  color: #409eff;
  margin-bottom: 4px;
  font-size: 15px;
}
.user-meta {
  color: #888;
  font-size: 14px;
  margin-top: 8px;
}
.profile-card {
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.06);
  margin-bottom: 24px;
}
.profile-btn {
  width: 100%;
  background: linear-gradient(90deg, #409eff 0%, #67c23a 100%);
  border: none;
  color: #fff;
  font-weight: bold;
  border-radius: 20px;
  box-shadow: 0 2px 8px 0 rgba(64,158,255,0.12);
}
.profile-btn:hover {
  filter: brightness(1.1);
}
</style> 