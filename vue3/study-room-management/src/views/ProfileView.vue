<template>
  <div class="profile">
    <h2>个人中心</h2>
    
    <div class="profile-container">
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="avatar-section">
          <div class="avatar">
            <img :src="userInfo.avatar || '/default-avatar.png'" alt="用户头像">
          </div>
          <button class="btn-upload">更换头像</button>
        </div>
        
        <div class="info-section">
          <div class="info-group">
            <label>用户名</label>
            <input type="text" v-model="userInfo.username" class="form-control">
          </div>
          
          <div class="info-group">
            <label>邮箱</label>
            <input type="email" v-model="userInfo.email" class="form-control">
          </div>
          
          <div class="info-group">
            <label>手机号</label>
            <input type="tel" v-model="userInfo.phone" class="form-control">
          </div>
          
          <button class="btn-save" @click="saveProfile">保存修改</button>
        </div>
      </div>

      <!-- 修改密码卡片 -->
      <div class="password-card">
        <h3>修改密码</h3>
        <div class="password-form">
          <div class="form-group">
            <label>当前密码</label>
            <input type="password" v-model="passwordForm.oldPassword" class="form-control">
          </div>
          
          <div class="form-group">
            <label>新密码</label>
            <input type="password" v-model="passwordForm.newPassword" class="form-control">
          </div>
          
          <div class="form-group">
            <label>确认新密码</label>
            <input type="password" v-model="passwordForm.confirmPassword" class="form-control">
          </div>
          
          <button class="btn-change-password" @click="changePassword">修改密码</button>
        </div>
      </div>

      <!-- 使用统计卡片 -->
      <div class="stats-card">
        <h3>使用统计</h3>
        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-value">{{ stats.totalHours }}</span>
            <span class="stat-label">总使用时长</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.totalReservations }}</span>
            <span class="stat-label">总预约次数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.completedReservations }}</span>
            <span class="stat-label">已完成预约</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.cancelledReservations }}</span>
            <span class="stat-label">已取消预约</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi, usageRecordApi } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()

// 检查登录状态
const checkLoginStatus = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  if (!userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

const userInfo = ref({
  username: '',
  email: '',
  phone: '',
  avatar: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const stats = ref({
  totalHours: 0,
  totalReservations: 0,
  completedReservations: 0,
  cancelledReservations: 0
})

// 获取用户信息
const fetchUserInfo = async () => {
  if (!checkLoginStatus()) return
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      const data = JSON.parse(userInfoStr)
      userInfo.value = data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 获取使用统计
const fetchStats = async () => {
  if (!checkLoginStatus()) return
  try {
    const records = await usageRecordApi.getUsageRecordsByUser(userInfo.value.username)
    const completed = records.filter(r => r.status === 'completed')
    const cancelled = records.filter(r => r.status === 'cancelled')
    
    stats.value = {
      totalHours: Math.floor(records.reduce((sum, r) => sum + r.duration, 0) / 60),
      totalReservations: records.length,
      completedReservations: completed.length,
      cancelledReservations: cancelled.length
    }
  } catch (error) {
    console.error('获取使用统计失败:', error)
  }
}

const saveProfile = async () => {
  if (!checkLoginStatus()) return
  try {
    await userApi.updateUser(userInfo.value.id, userInfo.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存个人信息失败:', error)
    ElMessage.error(error.message || '保存失败，请稍后重试')
  }
}

const changePassword = async () => {
  if (!checkLoginStatus()) return
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  try {
    await userApi.updateUser(userInfo.value.id, {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    ElMessage.success('密码修改成功')
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改失败，请稍后重试')
  }
}

onMounted(() => {
  fetchUserInfo()
  fetchStats()
})
</script>

<style scoped>
.profile {
  padding: var(--spacing-lg);
}

.profile h2 {
  margin: 0 0 var(--spacing-lg);
  color: var(--text-primary);
  font-size: var(--font-size-xl);
  font-weight: 600;
}

.profile-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-lg);
  margin-top: var(--spacing-lg);
}

.profile-card, .password-card, .stats-card {
  background: white;
  border-radius: 16px;
  padding: var(--spacing-lg);
  box-shadow: var(--card-shadow);
  transition: var(--transition-base);
}

.profile-card:hover, .password-card:hover, .stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.avatar-section {
  text-align: center;
  margin-bottom: var(--spacing-lg);
}

.avatar {
  width: 120px;
  height: 120px;
  margin: 0 auto var(--spacing-md);
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: var(--transition-base);
}

.avatar:hover {
  transform: scale(1.05);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.btn-upload {
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: var(--font-size-sm);
  font-weight: 500;
  transition: var(--transition-base);
}

.btn-upload:hover {
  background: #2980b9;
  transform: translateY(-1px);
}

.info-group {
  margin-bottom: var(--spacing-md);
}

.info-group label {
  display: block;
  margin-bottom: var(--spacing-xs);
  color: var(--text-primary);
  font-weight: 500;
}

.form-control {
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: var(--font-size-base);
  transition: var(--transition-base);
}

.form-control:focus {
  border-color: var(--primary-color);
  outline: none;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.btn-save, .btn-change-password {
  width: 100%;
  padding: var(--spacing-md);
  background: var(--success-color);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: var(--font-size-base);
  font-weight: 500;
  margin-top: var(--spacing-md);
  transition: var(--transition-base);
}

.btn-save:hover, .btn-change-password:hover {
  background: #27ae60;
  transform: translateY(-1px);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
  margin-top: var(--spacing-md);
}

.stat-item {
  text-align: center;
  padding: var(--spacing-md);
  background: #f8f9fa;
  border-radius: 12px;
  transition: var(--transition-base);
}

.stat-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

.stat-value {
  display: block;
  font-size: var(--font-size-2xl);
  font-weight: bold;
  color: var(--primary-color);
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .profile {
    padding: var(--spacing-md);
  }

  .profile h2 {
    font-size: var(--font-size-lg);
    margin-bottom: var(--spacing-md);
    text-align: center;
  }

  .profile-container {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
    margin-top: var(--spacing-md);
  }

  .profile-card, .password-card, .stats-card {
    padding: var(--spacing-md);
    border-radius: 12px;
  }

  .avatar {
    width: 100px;
    height: 100px;
  }

  .form-control {
    font-size: var(--font-size-sm);
    padding: 10px;
  }

  .btn-save, .btn-change-password {
    padding: 12px;
    font-size: var(--font-size-sm);
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-sm);
  }

  .stat-item {
    padding: var(--spacing-sm);
  }

  .stat-value {
    font-size: var(--font-size-xl);
  }

  .stat-label {
    font-size: var(--font-size-xs);
  }

  /* 修复表单在移动端的显示 */
  .info-group label,
  .form-group label {
    font-size: var(--font-size-sm);
    margin-bottom: 4px;
  }

  .form-group {
    margin-bottom: 12px;
  }

  /* 确保按钮在移动端有足够的点击区域 */
  .btn-upload,
  .btn-save,
  .btn-change-password {
    min-height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

/* 平板设备适配 */
@media screen and (min-width: 769px) and (max-width: 1024px) {
  .profile-container {
    grid-template-columns: repeat(2, 1fr);
  }

  .stats-card {
    grid-column: span 2;
  }
}
</style> 