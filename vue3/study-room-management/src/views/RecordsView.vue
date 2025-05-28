<template>
  <div class="records">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>预约记录</h2>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="filter" class="filter-form">
        <el-form-item label="筛选条件">
          <el-select v-model="filter.type" placeholder="选择筛选类型" style="width: 120px">
            <el-option label="日期" value="date" />
            <el-option label="座位号" value="seatNumber" />
            <el-option v-if="isAdmin" label="用户名" value="username" />
          </el-select>

          <!-- 用户名筛选（仅管理员可见） -->
          <el-input
            v-if="filter.type === 'username' && isAdmin"
            v-model="filter.username"
            placeholder="输入用户名"
            clearable
            style="width: 200px"
          />

          <!-- 日期筛选 -->
          <el-date-picker
            v-if="filter.type === 'date'"
            v-model="filter.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 200px"
          />

          <!-- 座位号筛选 -->
          <el-input
            v-if="filter.type === 'seatNumber'"
            v-model="filter.seatNumber"
            placeholder="输入座位号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 预约记录列表 -->
      <el-table :data="records" style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="80" :index="indexMethod" />
        <el-table-column prop="username" label="用户" width="120" v-if="isAdmin" />
        <el-table-column prop="seatNumber" label="座位号" width="100" />
        <el-table-column label="时间段" width="200">
          <template #default="{ row }">
            {{ formatTime(row.startTime, 'MM-DD HH:mm') }} - {{ formatTime(row.endTime, 'HH:mm') }}
          </template>
        </el-table-column>
        <el-table-column label="使用时长" width="120">
          <template #default="{ row }">
            {{ calculateDuration(row.startTime, row.endTime) }}分钟
          </template>
        </el-table-column>
        <el-table-column prop="price" label="费用" width="100">
          <template #default="{ row }">
            ¥{{ row.totalPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status, row.endTime)">
              {{ getStatusText(row.status, row.endTime) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime, 'YYYY-MM-DD HH:mm') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              size="small"
              @click="handleCancelReservation(row)"
            >
              取消预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { reservationApi } from '../api'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { userApi } from '../api'
import { seatApi } from '../api'

const router = useRouter()

// 获取用户角色
const isAdmin = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  return userInfo?.role === 'ADMIN'
})

// 获取当前用户信息
const currentUser = computed(() => {
  return JSON.parse(localStorage.getItem('userInfo') || 'null')
})

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filter = reactive({
  type: '', // 筛选类型：username/date/seatNumber
  username: '',
  date: '',
  seatNumber: ''
})

const records = ref([])

const getStatusType = (status, endTime) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'IN_USE':
      return 'primary'
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'info'
    default:
      return 'info'
  }
}

const getStatusText = (status, endTime) => {
  switch (status) {
    case 'PENDING':
      return '已预约'
    case 'IN_USE':
      return '使用中'
    case 'COMPLETED':
      return '已完成'
    case 'CANCELLED':
      return '已取消'
    default:
      return '未知'
  }
}

const formatTime = (time, fmt = 'YYYY-MM-DD HH:mm') => {
  if (!time) return ''
  return dayjs(time).format(fmt)
}

// 序号计算方法
const indexMethod = (index) => {
  return index + 1
}

// 计算使用时长
const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return 0
  const start = dayjs(startTime)
  const end = dayjs(endTime)
  return end.diff(start, 'minute')
}

// 获取预约记录
const fetchRecords = async () => {
  try {
    loading.value = true
    let data
    
    // 如果不是管理员，强制使用当前用户ID
    if (!isAdmin.value) {
      if (!currentUser.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      data = await reservationApi.getReservationsByUser(currentUser.value.id)
    } else {
      // 管理员可以根据筛选条件查询
      if (filter.type === 'username') {
        const userResponse = await userApi.getUserByUsername(filter.username)
        if (userResponse) {
          data = await reservationApi.getReservationsByUser(userResponse.id)
        }
      } else if (filter.type === 'date') {
        data = await reservationApi.getReservationsByTimeRange(
          `${filter.date}T00:00:00`,
          `${filter.date}T23:59:59`
        )
      } else if (filter.type === 'seatNumber') {
        data = await reservationApi.getReservationsBySeat(filter.seatNumber)
      } else {
        data = await reservationApi.getAllReservations()
      }
    }
    
    // 处理数据，获取座位信息和用户信息
    const processedRecords = await Promise.all(data.map(async (record) => {
      try {
        // 获取座位信息
        const seatInfo = await seatApi.getSeatById(record.seatId)
        // 获取用户信息
        const userInfo = await userApi.getUserById(record.userId)
        
        return {
          ...record,
          seatNumber: seatInfo.seatNumber || `座位${record.seatId}`,
          startTime: record.startTime || record.createTime,
          endTime: record.endTime || record.updateTime,
          totalPrice: record.totalPrice || 0,
          status: record.status || 'PENDING',
          username: userInfo?.username || '未知用户'
        }
      } catch (error) {
        console.error(`获取记录信息失败:`, error)
        return {
          ...record,
          seatNumber: `座位${record.seatId}`,
          startTime: record.startTime || record.createTime,
          endTime: record.endTime || record.updateTime,
          totalPrice: record.totalPrice || 0,
          status: record.status || 'PENDING',
          username: '未知用户'
        }
      }
    }))
    
    records.value = processedRecords
    total.value = records.value.length
  } catch (error) {
    console.error('获取预约记录失败:', error)
    ElMessage.error('获取预约记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (!filter.type) {
    ElMessage.warning('请选择筛选类型')
    return
  }

  const value = filter[filter.type]
  if (!value) {
    ElMessage.warning('请输入筛选内容')
    return
  }

  fetchRecords()
}

const resetFilter = () => {
  filter.type = ''
  filter.username = ''
  filter.date = ''
  filter.seatNumber = ''
  fetchRecords()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRecords()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRecords()
}

const handleCancelReservation = async (row) => {
  // 检查当前时间是否超过预约开始时间
  const now = dayjs()
  const startTime = dayjs(row.startTime)
  
  if (now.isAfter(startTime)) {
    ElMessage.warning('预约已开始，无法取消')
    return
  }
  
  try {
    // 弹出确认对话框
    await ElMessageBox.confirm(
      '确定要取消该预约吗？',
      '取消预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 用户点击确定，调用取消预约API
    await reservationApi.cancelReservation(row.id)
    ElMessage.success('预约已取消')
    // 刷新预约记录列表
    fetchRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error('取消预约失败，请稍后重试')
    }
  }
}

onMounted(() => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  fetchRecords()
  
  // 为表格添加响应式支持
  setTimeout(() => {
    const tableRows = document.querySelectorAll('.el-table__row')
    tableRows.forEach(row => {
      const cells = row.querySelectorAll('.el-table__cell')
      if (cells.length > 0) {
        // 设置单元格的data-label属性
        const labels = ['序号', '用户', '座位号', '时间段', '使用时长', '费用', '状态', '创建时间', '操作']
        cells.forEach((cell, index) => {
          if (index < labels.length) {
            const cellDiv = cell.querySelector('.cell')
            if (cellDiv) {
              cellDiv.setAttribute('data-label', labels[index])
            }
          }
        })
      }
    })
  }, 500)
})
</script>

<style scoped>
.records {
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.filter-form {
  margin-bottom: 24px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

:deep(.el-table) {
  --el-table-border-color: #ebeef5;
  --el-table-header-bg-color: #f8f9fa;
  border-radius: 12px;
  overflow: hidden;
  width: 100% !important;
  box-sizing: border-box;
}

:deep(.el-table th) {
  font-weight: 600;
  color: var(--text-primary);
  background-color: var(--el-table-header-bg-color) !important;
}

:deep(.el-table td) {
  color: var(--text-primary);
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: rgba(52, 152, 219, 0.05);
}

:deep(.el-tag) {
  padding: 4px 12px;
  font-weight: 500;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 状态标签颜色 */
:deep(.el-tag--success) {
  background-color: rgba(46, 204, 113, 0.1);
  border-color: rgba(46, 204, 113, 0.2);
  color: #27ae60;
}

:deep(.el-tag--warning) {
  background-color: rgba(241, 196, 15, 0.1);
  border-color: rgba(241, 196, 15, 0.2);
  color: #f39c12;
}

:deep(.el-tag--danger) {
  background-color: rgba(231, 76, 60, 0.1);
  border-color: rgba(231, 76, 60, 0.2);
  color: #c0392b;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .records {
    padding: 12px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;
  }

  .card-header h2 {
    font-size: 20px;
  }

  /* 筛选表单优化 */
  .filter-form {
    padding: 12px;
    margin-bottom: 16px;
  }

  :deep(.el-form--inline) {
    flex-direction: column;
    gap: 12px;
  }

  :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
    width: 100%;
  }

  :deep(.el-form-item__content) {
    flex-wrap: wrap;
    gap: 8px;
  }

  :deep(.el-select),
  :deep(.el-input),
  :deep(.el-date-picker) {
    width: 100% !important;
  }

  :deep(.el-button) {
    width: 100%;
    margin-left: 0;
  }

  /* 移动端表格优化 - 完全重写为卡片式布局 */
  :deep(.el-table) {
    display: block;
    width: 100% !important;
    overflow-x: hidden !important;
  }

  :deep(.el-table__header-wrapper) {
    display: none;
  }

  :deep(.el-table__body-wrapper) {
    display: block;
    width: 100% !important;
    overflow-x: hidden !important;
  }

  :deep(.el-scrollbar__wrap) {
    width: 100% !important;
    height: auto !important;
    overflow: visible !important;
  }

  :deep(.el-scrollbar__view) {
    width: 100% !important;
  }

  :deep(.el-table__body) {
    width: 100% !important;
    display: block;
  }

  :deep(.el-table__row) {
    width: 100% !important;
    display: block;
    margin-bottom: 16px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
  }

  :deep(.el-table__cell) {
    display: block;
    width: 100% !important;
    padding: 12px 16px !important;
    border-bottom: 1px solid #f0f0f0;
    box-sizing: border-box;
  }

  :deep(.el-table__cell:last-child) {
    border-bottom: none;
  }

  :deep(.el-table__cell .cell) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100% !important;
    white-space: normal;
    line-height: 1.5;
    padding: 0 !important;
    box-sizing: border-box;
  }

  :deep(.el-table__cell .cell::before) {
    content: attr(data-label);
    font-weight: 500;
    color: #606266;
    flex-shrink: 0;
    margin-right: 12px;
    min-width: 80px;
  }

  :deep(.el-table__cell:last-child .cell::before) {
    display: none;
  }

  :deep(.el-table__cell:last-child .cell) {
    justify-content: flex-end;
    padding-top: 8px;
    padding-bottom: 8px;
  }

  :deep(.el-table__cell:last-child .cell .el-button) {
    margin-left: 0;
    width: 100%;
  }

  /* 修复卡片不占满宽度的问题 */
  :deep(.el-card) {
    width: 100% !important;
    margin: 0 !important;
    box-sizing: border-box !important;
  }
  
  :deep(.el-card__body) {
    width: 100% !important;
    box-sizing: border-box;
    padding: 12px !important;
  }
  
  :deep(.el-table__inner-wrapper) {
    width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
  }
  
  :deep(.el-table__body td.el-table__cell) {
    max-width: 100% !important;
    min-width: 100% !important;
  }
  
  :deep(.el-table__body-wrapper .el-scrollbar__view) {
    width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
  }
  
  /* 标签样式优化 */
  :deep(.el-tag) {
    font-size: 12px;
    padding: 0 8px;
    height: 24px;
    line-height: 22px;
    max-width: 100%;
    box-sizing: border-box;
  }

  /* 分页组件优化 */
  .pagination {
    justify-content: center;
    margin-top: 16px;
  }

  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
  }

  :deep(.el-pagination__sizes) {
    display: none !important;
  }

  :deep(.el-pagination__jump) {
    margin-left: 0 !important;
  }
  
  /* 确保单元格内容显示标签 */
  :deep(.el-table__row .el-table__cell:nth-child(1) .cell::before) {
    content: "序号: ";
  }

  :deep(.el-table__row .el-table__cell:nth-child(2) .cell::before) {
    content: "用户: ";
  }

  :deep(.el-table__row .el-table__cell:nth-child(3) .cell::before) {
    content: "座位号: ";
  }

  :deep(.el-table__row .el-table__cell:nth-child(4) .cell::before) {
    content: "时间段: ";
  }

  :deep(.el-table__row .el-table__cell:nth-child(5) .cell::before) {
    content: "使用时长: ";
  }

  :deep(.el-table__row .el-table__cell:nth-child(6) .cell::before) {
    content: "费用: ";
  }

  :deep(.el-table__row .el-table__cell:nth-child(7) .cell::before) {
    content: "状态: ";
  }

  :deep(.el-table__row .el-table__cell:nth-child(8) .cell::before) {
    content: "创建时间: ";
  }
}

/* 平板设备适配 */
@media screen and (min-width: 769px) and (max-width: 1024px) {
  .records {
    padding: 16px;
  }

  :deep(.el-table) {
    font-size: 14px;
  }

  .filter-form {
    padding: 16px;
  }
}

/* 修复表格容器宽度问题 */
:deep(.el-card) {
  width: 100% !important;
  box-sizing: border-box;
  padding: 0 !important;
}

:deep(.el-card__body) {
  width: 100% !important;
  box-sizing: border-box;
  padding: 16px !important;
}

/* 确保表格填满容器 */
:deep(.el-table) {
  width: 100% !important;
  box-sizing: border-box;
}
</style>