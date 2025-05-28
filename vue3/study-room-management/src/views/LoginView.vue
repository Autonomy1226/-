<template>
  <div class="login">
    <el-card class="login-container">
      <template #header>
        <h2>登录</h2>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="submit-btn"
          >
            登录
          </el-button>
        </el-form-item>
        
        <div class="form-footer">
          <router-link to="/register">
            <el-button link>注册账号</el-button>
          </router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { userApi } from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const loginData = {
      username: form.username,
      password: form.password
    }
    
    console.log('登录请求数据:', loginData)
    const response = await userApi.login(loginData)
    console.log('登录响应数据:', response)
    
    // 检查响应数据的结构
    if (response) {
      // 构造用户信息对象
      const userInfo = {
        id: response.id,
        username: response.username,
        email: response.email,
        phone: response.phone,
        role: response.role || 'USER', // 确保有默认角色
        createTime: response.createTime,
        updateTime: response.updateTime
      }
      
      console.log('保存的用户信息:', userInfo)
      
      // 存储用户信息
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      
      // 触发登录状态更新事件
      window.dispatchEvent(new CustomEvent('login-state-change', {
        detail: { userInfo }
      }))
      
      ElMessage.success('登录成功')
      
      // 使用 replace 而不是 push，防止用户点击返回按钮时回到登录页
      router.replace('/')
    } else {
      throw new Error('登录响应数据格式错误')
    }
  } catch (error) {
    console.error('登录失败:', error)
    if (error.response) {
      // 服务器返回错误
      ElMessage.error(error.response.data?.message || '登录失败，请检查用户名和密码')
    } else if (error.request) {
      // 请求发出但没有收到响应
      ElMessage.error('无法连接到服务器，请检查网络连接')
    } else {
      // 其他错误
      ElMessage.error(error.message || '登录失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  padding: var(--spacing-md);
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 16px;
  box-shadow: var(--card-shadow);
  transition: var(--transition-base);
}

.login-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.login-container :deep(.el-card__header) {
  text-align: center;
  padding: var(--spacing-lg);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.login-container h2 {
  margin: 0;
  color: var(--text-primary);
  font-size: var(--font-size-xl);
  font-weight: 600;
}

.login-container :deep(.el-form) {
  padding: var(--spacing-lg);
}

.login-container :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary);
  padding-bottom: var(--spacing-xs);
}

.login-container :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6;
  transition: var(--transition-base);
}

.login-container :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color);
}

.login-container :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color);
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: var(--font-size-base);
  font-weight: 500;
  margin-top: var(--spacing-md);
}

.form-footer {
  text-align: center;
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-md);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .login {
    padding: var(--spacing-sm);
  }

  .login-container {
    max-width: 100%;
  }

  .login-container :deep(.el-card__header) {
    padding: var(--spacing-md);
  }

  .login-container h2 {
    font-size: var(--font-size-lg);
  }

  .login-container :deep(.el-form) {
    padding: var(--spacing-md);
  }

  .submit-btn {
    height: 40px;
    font-size: var(--font-size-sm);
  }
}

/* 平板设备适配 */
@media screen and (min-width: 769px) and (max-width: 1024px) {
  .login-container {
    max-width: 360px;
  }
}
</style>