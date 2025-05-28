import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/study-rooms',
      name: 'study-rooms',
      component: () => import('../views/StudyRoomsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/seats',
      name: 'seats',
      component: () => import('../views/SeatsView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/reservations',
      name: 'reservations',
      component: () => import('../views/ReservationsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/records',
      name: 'records',
      component: () => import('../views/RecordsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { guest: true }
    }
  ]
})

// 添加路由守卫
router.beforeEach((to, from, next) => {
  const userInfo = localStorage.getItem('userInfo')
  const isAuthenticated = !!userInfo
  const userRole = userInfo ? JSON.parse(userInfo).role : null
  
  // 需要登录的页面
  if (to.meta.requiresAuth && !isAuthenticated) {
    ElMessage.warning('请先登录')
    next('/login')
  }
  // 需要管理员权限的页面
  else if (to.meta.requiresAdmin && userRole !== 'ADMIN') {
    ElMessage.warning('需要管理员权限')
    next('/')
  }
  // 游客页面（登录和注册）
  else if (to.meta.guest && isAuthenticated) {
    next('/')
  }
  else {
    next()
  }
})

export default router