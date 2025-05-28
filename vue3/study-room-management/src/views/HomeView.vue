<template>
  <div class="home">
    <div class="welcome-section">
      <h2>欢迎使用自习室管理系统</h2>
      <p class="subtitle">高效管理，智慧学习</p>
    </div>
    
    <div class="dashboard">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><School /></el-icon>
        </div>
        <h3>自习室状态</h3>
        <div class="stat-content">
          <div class="stat-item">
            <span class="label">开放自习室</span>
            <span class="value">{{ stats.openRooms }}</span>
          </div>
          <div class="stat-item">
            <span class="label">可用座位</span>
            <span class="value">{{ stats.availableSeats }}</span>
          </div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><Calendar /></el-icon>
        </div>
        <h3>今日统计</h3>
        <div class="stat-content">
          <div class="stat-item">
            <span class="label">预约人数</span>
            <span class="value">{{ stats.todayReservations }}</span>
          </div>
          <div class="stat-item">
            <span class="label">使用中</span>
            <span class="value">{{ stats.inUseSeats }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <el-button type="primary" @click="$router.push('/reservations')">
        <el-icon><Clock /></el-icon>
        立即预约
      </el-button>
      <el-button type="success" @click="$router.push('/records')">
        <el-icon><Document /></el-icon>
        查看记录
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { studyRoomApi, seatApi, reservationApi } from '../api'
import dayjs from 'dayjs'
import { School, Calendar, Clock, Document } from '@element-plus/icons-vue'

const stats = ref({
  openRooms: 0,
  availableSeats: 0,
  todayReservations: 0,
  inUseSeats: 0
})

// 获取统计数据
const fetchStats = async () => {
  try {
    // 获取所有自习室
    const rooms = await studyRoomApi.getAllRooms()
    // 统计开放的自习室数量
    stats.value.openRooms = rooms.filter(room => room.status === 'OPEN').length

    // 获取所有座位
    const allSeats = []
    for (const room of rooms) {
      const seats = await seatApi.getSeatsByRoom(room.id)
      allSeats.push(...seats)
    }
    // 统计可用座位数量
    stats.value.availableSeats = allSeats.filter(seat => seat.status === 'AVAILABLE').length

    // 获取今天的日期范围
    const today = dayjs().format('YYYY-MM-DD')
    const startTime = `${today}T00:00:00`
    const endTime = `${today}T23:59:59`

    // 获取今天的预约记录
    const todayReservations = await reservationApi.getReservationsByTimeRange(startTime, endTime)
    stats.value.todayReservations = todayReservations.length

    // 统计使用中的座位数量
    stats.value.inUseSeats = todayReservations.filter(reservation => 
      reservation.status === 'IN_USE'
    ).length

  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.home {
  padding: var(--spacing-lg);
}

.welcome-section {
  background: linear-gradient(135deg, var(--primary-color), #2980b9);
  color: white;
  padding: var(--spacing-xl);
  border-radius: 16px;
  margin-bottom: var(--spacing-xl);
  box-shadow: var(--card-shadow);
}

.welcome-section h1 {
  font-size: var(--font-size-2xl);
  margin-bottom: var(--spacing-md);
  font-weight: 600;
}

.welcome-section p {
  font-size: var(--font-size-lg);
  opacity: 0.9;
  margin-bottom: var(--spacing-lg);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.action-card {
  background: white;
  padding: var(--spacing-lg);
  border-radius: 16px;
  box-shadow: var(--card-shadow);
  transition: var(--transition-base);
  text-align: center;
  cursor: pointer;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.action-card i {
  font-size: 2.5rem;
  color: var(--primary-color);
  margin-bottom: var(--spacing-md);
}

.action-card h3 {
  font-size: var(--font-size-lg);
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.action-card p {
  color: var(--text-secondary);
  font-size: var(--font-size-base);
}

.stats-section {
  background: white;
  padding: var(--spacing-lg);
  border-radius: 16px;
  box-shadow: var(--card-shadow);
}

.stats-section h2 {
  font-size: var(--font-size-xl);
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
}

.stat-card {
  background: #f8f9fa;
  padding: var(--spacing-lg);
  border-radius: 12px;
  text-align: center;
  transition: var(--transition-base);
}

.stat-card:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

.stat-value {
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
  .home {
    padding: var(--spacing-md);
  }

  .welcome-section {
    padding: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);
  }

  .welcome-section h1 {
    font-size: var(--font-size-xl);
  }

  .welcome-section p {
    font-size: var(--font-size-base);
  }

  .quick-actions {
    gap: var(--spacing-md);
  }

  .action-card {
    padding: var(--spacing-md);
  }

  .action-card i {
    font-size: 2rem;
  }

  .action-card h3 {
    font-size: var(--font-size-base);
  }

  .action-card p {
    font-size: var(--font-size-sm);
  }

  .stats-section {
    padding: var(--spacing-md);
  }

  .stats-section h2 {
    font-size: var(--font-size-lg);
  }

  .stats-grid {
    gap: var(--spacing-md);
  }

  .stat-card {
    padding: var(--spacing-md);
  }

  .stat-value {
    font-size: var(--font-size-xl);
  }
}

/* 平板设备适配 */
@media screen and (min-width: 769px) and (max-width: 1024px) {
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style> 