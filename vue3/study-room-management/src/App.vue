<template>
  <el-container class="app-container">
    <el-header>
      <div class="header-content">
        <div class="logo">自习室管理系统</div>
        <!-- 移动端汉堡菜单按钮 -->
        <el-button 
          class="menu-toggle" 
          @click="toggleMobileMenu"
          v-if="isMobile"
        >
          <el-icon><Menu /></el-icon>
        </el-button>
        <!-- PC端导航菜单 -->
        <el-menu
          v-if="!isMobile"
          mode="horizontal"
          :router="true"
          :default-active="activeMenu"
          class="nav-menu"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/study-rooms">自习室</el-menu-item>
          <el-menu-item index="/seats" v-if="isAdmin">
            <span>座位管理</span>
          </el-menu-item>
          <el-menu-item index="/reservations">预约</el-menu-item>
          <el-menu-item index="/records">订单记录</el-menu-item>
        </el-menu>
        <div class="user-info">
          <!-- 未登录状态 -->
          <template v-if="!isAuthenticated">
            <el-button type="primary" link @click="handleLogin">登录</el-button>
            <el-button type="primary" link @click="handleRegister">注册</el-button>
          </template>
          <!-- 已登录状态 -->
          <template v-else>
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                {{ username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 移动端导航抽屉 -->
    <el-drawer
      v-model="mobileMenuVisible"
      direction="ltr"
      size="80%"
      :with-header="false"
      class="mobile-menu-drawer"
    >
      <div class="mobile-menu-content">
        <div class="mobile-menu-header">
          <div class="logo">自习室管理系统</div>
          <el-button 
            class="close-menu" 
            @click="mobileMenuVisible = false"
          >
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <el-menu
          mode="vertical"
          :router="true"
          :default-active="activeMenu"
          class="mobile-nav-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/study-rooms">
            <el-icon><School /></el-icon>
            <span>自习室</span>
          </el-menu-item>
          <el-menu-item index="/seats" v-if="isAdmin">
            <el-icon><Position /></el-icon>
            <span>座位管理</span>
          </el-menu-item>
          <el-menu-item index="/reservations">
            <el-icon><Calendar /></el-icon>
            <span>预约</span>
          </el-menu-item>
          <el-menu-item index="/records">
            <el-icon><Document /></el-icon>
            <span>订单记录</span>
          </el-menu-item>
        </el-menu>
        <div class="mobile-user-info">
          <template v-if="!isAuthenticated">
            <el-button type="primary" @click="handleLogin">登录</el-button>
            <el-button type="primary" @click="handleRegister">注册</el-button>
          </template>
          <template v-else>
            <div class="mobile-user-profile">
              <el-icon><User /></el-icon>
              <span>{{ username }}</span>
            </div>
            <el-button type="primary" @click="handleCommand('profile')">
              个人中心
            </el-button>
            <el-button type="danger" @click="handleCommand('logout')">
              退出登录
            </el-button>
          </template>
        </div>
      </div>
    </el-drawer>

    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowDown, 
  User, 
  SwitchButton, 
  Menu, 
  Close,
  HomeFilled,
  School,
  Position,
  Calendar,
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 移动端菜单控制
const isMobile = ref(false)
const mobileMenuVisible = ref(false)

// 检查是否为移动设备
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 监听窗口大小变化
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// 切换移动端菜单
const toggleMobileMenu = () => {
  mobileMenuVisible.value = !mobileMenuVisible.value
}

// 处理菜单选择
const handleMenuSelect = () => {
  mobileMenuVisible.value = false
}

// 从localStorage获取用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
const isAuthenticated = computed(() => !!userInfo.value)
const username = computed(() => userInfo.value?.username || '未登录')
const activeMenu = computed(() => route.path)

const isAdmin = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  console.log('App.vue - 检查管理员权限 - 当前用户信息:', userInfo)
  const isAdminUser = userInfo?.role === 'ADMIN'
  console.log('App.vue - 是否为管理员:', isAdminUser)
  return isAdminUser
})

// 监听登录状态变化
const handleLoginStateChange = (event) => {
  console.log('App.vue - 登录状态变化事件:', event.detail)
  if (event.detail.userInfo) {
    userInfo.value = event.detail.userInfo
  } else {
    const newUserInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
    userInfo.value = newUserInfo
  }
  console.log('App.vue - 更新后的用户信息:', userInfo.value)
  // 强制刷新页面
  window.location.reload()
}

onMounted(() => {
  window.addEventListener('login-state-change', handleLoginStateChange)
  // 初始化时检查用户信息
  const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  console.log('App.vue - 初始化用户信息:', currentUserInfo)
  userInfo.value = currentUserInfo
  // 强制更新 isAdmin 计算属性
  nextTick(() => {
    console.log('App.vue - 初始化后的管理员状态:', isAdmin.value)
  })
})

onUnmounted(() => {
  window.removeEventListener('login-state-change', handleLoginStateChange)
})

// 处理登录按钮点击
const handleLogin = () => {
  router.push('/login')
}

// 处理注册按钮点击
const handleRegister = () => {
  router.push('/register')
}

// 处理用户菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      // 清除所有本地存储的用户信息
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      // 更新用户信息状态
      userInfo.value = null
      // 触发登录状态变化事件
      window.dispatchEvent(new CustomEvent('login-state-change', {
        detail: { userInfo: null }
      }))
      ElMessage.success('退出成功')
      // 强制刷新页面
      window.location.href = '/login'
      break
  }
}
</script>

<style>
:root {
  --primary-color: #3498db;
  --success-color: #2ecc71;
  --warning-color: #f1c40f;
  --danger-color: #e74c3c;
  --text-primary: #2c3e50;
  --text-secondary: #7f8c8d;
  --bg-color: #f8f9fa;
  --card-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  --transition-base: all 0.3s ease;
  
  /* 添加字体大小变量 */
  --font-size-xs: 12px;
  --font-size-sm: 14px;
  --font-size-base: 16px;
  --font-size-lg: 18px;
  --font-size-xl: 20px;
  --font-size-2xl: 24px;
  
  /* 添加间距变量 */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
}

body {
  margin: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: var(--bg-color);
  color: var(--text-primary);
  font-size: var(--font-size-base);
  line-height: 1.5;
}

.app-container {
  min-height: 100vh;
}

.el-header {
  background: white;
  box-shadow: var(--card-shadow);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-color);
  margin-right: 40px;
  letter-spacing: -0.5px;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
}

.el-menu-item {
  font-weight: 500;
  transition: var(--transition-base);
}

.el-menu-item:hover {
  color: var(--primary-color) !important;
}

.el-menu-item.is-active {
  color: var(--primary-color) !important;
  font-weight: 600;
}

.user-info {
  margin-left: 20px;
  display: flex;
  gap: 12px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: var(--text-primary);
  font-weight: 500;
  transition: var(--transition-base);
}

.user-dropdown:hover {
  color: var(--primary-color);
}

.user-dropdown .el-icon {
  margin-left: 6px;
  transition: var(--transition-base);
}

.el-main {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 移动端导航样式 */
.menu-toggle {
  display: none;
  padding: 8px;
  font-size: 20px;
  border: none;
  background: transparent;
  color: var(--text-primary);
}

.mobile-menu-drawer {
  --el-drawer-padding-primary: 0;
}

.mobile-menu-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.mobile-menu-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.close-menu {
  padding: 8px;
  font-size: 20px;
  border: none;
  background: transparent;
  color: var(--text-primary);
}

.mobile-nav-menu {
  flex: 1;
  border-right: none;
}

.mobile-nav-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.mobile-nav-menu :deep(.el-menu-item .el-icon) {
  margin-right: 8px;
  font-size: 18px;
}

.mobile-user-info {
  padding: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.mobile-user-profile .el-icon {
  font-size: 18px;
  color: var(--primary-color);
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .menu-toggle {
    display: block;
  }

  .nav-menu {
    display: none;
  }

  .header-content {
    padding: 0 12px;
  }

  .logo {
    font-size: 18px;
    margin-right: 0;
  }

  .user-info {
    display: none;
  }
}

/* 平板设备适配 */
@media screen and (min-width: 769px) and (max-width: 1024px) {
  .header-content {
    padding: 0 var(--spacing-lg);
  }

  .el-main {
    padding: var(--spacing-lg);
  }
}

/* 全局卡片样式优化 */
.el-card {
  border: none;
  border-radius: 12px;
  box-shadow: var(--card-shadow);
  transition: var(--transition-base);
  margin-bottom: var(--spacing-lg);
}

.el-card__header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

/* 全局按钮样式优化 */
.el-button {
  font-weight: 500;
  border-radius: 8px;
  transition: var(--transition-base);
  height: auto;
  line-height: 1.4;
}

/* 全局表单样式优化 */
.el-form-item {
  margin-bottom: var(--spacing-lg);
}

.el-form-item__label {
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
}

.el-input__inner {
  border-radius: 8px;
  transition: var(--transition-base);
  height: auto;
  line-height: 1.4;
  padding: var(--spacing-sm) var(--spacing-md);
}

/* 全局表格样式优化 */
.el-table {
  border-radius: 12px;
  overflow: hidden;
  font-size: var(--font-size-sm);
}

.el-table th {
  background-color: var(--bg-color) !important;
  font-weight: 600;
  padding: var(--spacing-md);
}

.el-table td {
  padding: var(--spacing-md);
}

/* 全局标签样式优化 */
.el-tag {
  border-radius: 6px;
  font-weight: 500;
  line-height: 1.4;
}

/* 全局对话框样式优化 */
.el-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.el-dialog__header {
  padding: var(--spacing-lg);
  margin: 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.el-dialog__body {
  padding: var(--spacing-lg);
}

/* 全局分页样式优化 */
.el-pagination {
  margin-top: var(--spacing-lg);
  justify-content: flex-end;
  gap: var(--spacing-sm);
}

/* 全局加载动画优化 */
.el-loading-mask {
  backdrop-filter: blur(2px);
}
</style>