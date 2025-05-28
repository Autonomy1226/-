<template>
  <div class="reservations">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>预约座位</h2>
        </div>
      </template>

      <!-- 预约表单 -->
      <el-form :model="form" label-width="100px" class="reservation-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="自习室">
              <el-select 
                v-model="form.roomId" 
                placeholder="选择自习室"
                @change="handleRoomChange">
                <el-option
                  v-for="room in rooms"
                  :key="room.id"
                  :label="room.name"
                  :value="room.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="日期">
              <el-date-picker
                v-model="form.date"
                type="date"
                placeholder="选择日期"
                :disabled-date="disabledDate"
                value-format="YYYY-MM-DD"
                @change="handleDateChange"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="开始时间">
              <el-time-picker
                v-model="form.startTime"
                placeholder="选择开始时间"
                format="HH:mm"
                :disabled-hours="disabledHours"
                :disabled-minutes="disabledMinutes"
                @change="handleTimeChange"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="结束时间">
              <el-time-picker
                v-model="form.endTime"
                placeholder="选择结束时间"
                format="HH:mm"
                :disabled-hours="disabledEndHours"
                :disabled-minutes="disabledEndMinutes"
                @change="handleTimeChange"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 座位选择区域 -->
        <div v-if="form.roomId && form.date && form.startTime && form.endTime" class="seat-selection">
          <h3>选择座位</h3>
          <div class="seat-grid">
            <el-card
              v-for="seat in availableSeats"
              :key="seat.id"
              :class="['seat-card', { 
                selected: selectedSeat?.id === seat.id,
                reserved: seat.status === 'reserved'
              }]"
              @click="selectSeat(seat)"
            >
              <div class="seat-number">{{ seat.number }}</div>
              <div class="seat-status">
                <el-tag :type="getSeatStatusType(seat.status)">
                  {{ seat.statusText }}
                </el-tag>
              </div>
              <div v-if="seat.reservationTimes && seat.reservationTimes.length > 0" class="reservation-times">
                <div v-for="(time, index) in seat.reservationTimes" :key="index" class="time-slot">
                  {{ time.start }} - {{ time.end }}
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 预约信息确认 -->
        <div v-if="selectedSeat" class="reservation-summary">
          <h3>预约信息确认</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="自习室">
              {{ selectedRoom?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="座位号">
              {{ selectedSeat.number }}
            </el-descriptions-item>
            <el-descriptions-item label="使用日期">
              {{ form.date }}
            </el-descriptions-item>
            <el-descriptions-item label="使用时间">
              {{ formatTime(form.startTime) }} - {{ formatTime(form.endTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="使用时长">
              {{ duration }}分钟
            </el-descriptions-item>
            <el-descriptions-item label="费用">
              <span class="price">¥{{ calculatePrice() }}</span>
            </el-descriptions-item>
          </el-descriptions>

          <div class="form-actions">
            <el-button type="primary" @click="submitReservation" :loading="loading">
              确认预约
            </el-button>
          </div>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { reservationApi, studyRoomApi, seatApi } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = ref({
  roomId: '',
  date: '',
  startTime: null,
  endTime: null
})

const loading = ref(false)
const selectedSeat = ref(null)

const rooms = ref([
  { id: 1, name: '第一自习室', pricePerHour: 5 },
  { id: 2, name: '第二自习室', pricePerHour: 6 }
])

const availableSeats = ref([])

const selectedRoom = computed(() => {
  return rooms.value.find(room => room.id === form.value.roomId)
})

const duration = computed(() => {
  if (!form.value.startTime || !form.value.endTime) return 0
  const start = dayjs(form.value.startTime)
  const end = dayjs(form.value.endTime)
  return end.diff(start, 'minute')
})

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 禁用非营业时间
const disabledHours = () => {
  const now = dayjs()
  const currentHour = now.hour()
  const currentMinute = now.minute()
  
  // 如果是今天，禁用当前时间之前的小时
  if (form.value.date === now.format('YYYY-MM-DD')) {
    return Array.from({ length: 24 }, (_, i) => i).filter(h => 
      h < currentHour || h < 8 || h > 22
    )
  }
  
  // 其他日期只禁用非营业时间
  return Array.from({ length: 24 }, (_, i) => i).filter(h => h < 8 || h > 22)
}

const disabledMinutes = (hour) => {
  const now = dayjs()
  const currentHour = now.hour()
  const currentMinute = now.minute()
  
  // 如果是今天且是当前小时，禁用当前时间之前的分钟
  if (form.value.date === now.format('YYYY-MM-DD') && hour === currentHour) {
    return Array.from({ length: 60 }, (_, i) => i).filter(m => m < currentMinute)
  }
  
  // 其他时间段全部可选
  return []
}

const disabledEndHours = () => {
  if (!form.value.startTime) return []
  const start = dayjs(form.value.startTime)
  const startHour = start.hour()
  
  // 如果是今天，需要考虑当前时间
  const now = dayjs()
  if (form.value.date === now.format('YYYY-MM-DD')) {
    const currentHour = now.hour()
    return Array.from({ length: 24 }, (_, i) => i).filter(h => 
      h < startHour || h > 22 || (h === currentHour && now.minute() >= 55)
    )
  }
  
  return Array.from({ length: 24 }, (_, i) => i).filter(h => h < startHour || h > 22)
}

const disabledEndMinutes = (hour) => {
  if (!form.value.startTime) return []
  const start = dayjs(form.value.startTime)
  const startHour = start.hour()
  const startMinute = start.minute()
  
  if (hour === startHour) {
    // 确保结束时间至少比开始时间晚30分钟
    return Array.from({ length: 60 }, (_, i) => i).filter(m => 
      m <= startMinute || m < startMinute + 30
    )
  }
  return []
}

// 获取当前用户信息
const currentUser = computed(() => {
  return JSON.parse(localStorage.getItem('userInfo') || 'null')
})

// 检查登录状态
const checkLoginStatus = () => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

// 获取自习室列表
const fetchRooms = async () => {
  try {
    const data = await studyRoomApi.getAllRooms()
    rooms.value = data.map(room => ({
      id: room.id,
      name: room.roomName,
      pricePerHour: room.pricePerHour || 5
    }))
  } catch (error) {
    console.error('获取自习室列表失败:', error)
    ElMessage.error('获取自习室列表失败')
  }
}

// 获取可用座位
const fetchAvailableSeats = async () => {
  if (!form.value.roomId || !form.value.date || !form.value.startTime || !form.value.endTime) {
    availableSeats.value = []
    return
  }
  
  try {
    // 获取该自习室的所有座位
    const allSeats = await seatApi.getSeatsByRoom(form.value.roomId)
    console.log('获取到的座位列表:', allSeats)
    
    // 获取该日期已预约的座位
    const startTime = `${form.value.date}T${formatTime(form.value.startTime)}:00`
    const endTime = `${form.value.date}T${formatTime(form.value.endTime)}:00`
    const reservations = await reservationApi.getReservationsByTimeRange(startTime, endTime)
    console.log('获取到的预约记录:', reservations)
    
    // 处理座位状态
    availableSeats.value = allSeats.map(seat => {
      // 首先检查座位的原始状态
      if (seat.status === 'UNAVAILABLE') {
        return {
          id: seat.id,
          number: seat.seatNumber,
          status: 'unavailable',
          statusText: '不可用',
          reservationTimes: [],
          originalStatus: seat.status
        }
      }

      // 获取该座位的所有预约记录
      const seatReservations = reservations.filter(reservation => 
        reservation.seatId === seat.id && reservation.status === 'PENDING'
      )
      console.log(`座位 ${seat.seatNumber} 的预约记录:`, seatReservations)
      
      // 检查是否有时间冲突
      const hasConflict = seatReservations.some(reservation => {
        const reservationStart = dayjs(reservation.startTime)
        const reservationEnd = dayjs(reservation.endTime)
        const selectedStart = dayjs(startTime)
        const selectedEnd = dayjs(endTime)
        
        // 检查时间是否重叠
        const isOverlapping = (
          (selectedStart.isAfter(reservationStart) && selectedStart.isBefore(reservationEnd)) ||
          (selectedEnd.isAfter(reservationStart) && selectedEnd.isBefore(reservationEnd)) ||
          (selectedStart.isBefore(reservationStart) && selectedEnd.isAfter(reservationEnd)) ||
          (selectedStart.isSame(reservationStart)) ||
          (selectedEnd.isSame(reservationEnd)) ||
          (selectedStart.isAfter(reservationStart) && selectedEnd.isBefore(reservationEnd))
        )
        
        console.log(`检查座位 ${seat.seatNumber} 的时间冲突:`, {
          reservationTime: `${reservationStart.format('HH:mm')} - ${reservationEnd.format('HH:mm')}`,
          selectedTime: `${selectedStart.format('HH:mm')} - ${selectedEnd.format('HH:mm')}`,
          isOverlapping
        })
        
        return isOverlapping
      })
      
      // 获取该座位的所有预约时间段，只显示PENDING状态的预约
      const reservationTimes = seatReservations.map(reservation => ({
        start: dayjs(reservation.startTime).format('HH:mm'),
        end: dayjs(reservation.endTime).format('HH:mm')
      }))
      
      return {
        id: seat.id,
        number: seat.seatNumber,
        status: hasConflict ? 'reserved' : 'available',
        statusText: hasConflict ? '已被预约' : '可用',
        reservationTimes: reservationTimes,
        originalStatus: seat.status
      }
    })
    
    console.log('处理后的座位列表:', availableSeats.value)
  } catch (error) {
    console.error('获取座位列表失败:', error)
    ElMessage.error('获取座位列表失败')
  }
}

// 监听自习室和日期变化
const handleRoomChange = () => {
  selectedSeat.value = null
  if (form.value.date && form.value.startTime && form.value.endTime) {
    fetchAvailableSeats()
  }
}

const handleDateChange = () => {
  selectedSeat.value = null
  if (form.value.startTime && form.value.endTime) {
    fetchAvailableSeats()
  }
}

const handleTimeChange = () => {
  if (form.value.startTime && form.value.endTime) {
    const now = dayjs();
    // 保证start和end的日期部分和form.value.date一致
    const start = dayjs(`${form.value.date} ${dayjs(form.value.startTime).format('HH:mm')}`);
    const end = dayjs(`${form.value.date} ${dayjs(form.value.endTime).format('HH:mm')}`);

    // 只在选择当天时校验
    if (form.value.date === now.format('YYYY-MM-DD')) {
      const fiveMinutesAgo = now.subtract(5, 'minute');
      if (start.isBefore(fiveMinutesAgo)) {
        ElMessage.warning('开始时间不能早于当前时间前5分钟');
        form.value.startTime = null;
        return;
      }
    }

    if (end.isBefore(start)) {
      ElMessage.warning('结束时间不能早于开始时间');
      form.value.endTime = null;
    } else {
      const duration = end.diff(start, 'minute');
      if (duration < 30) {
        ElMessage.warning('预约时长不能少于30分钟');
        form.value.endTime = null;
      } else {
        fetchAvailableSeats();
      }
    }
  }
}

const selectSeat = (seat) => {
  if (seat.status === 'reserved' || seat.status === 'unavailable') {
    ElMessage.warning(seat.status === 'reserved' ? '该座位已被预约' : '该座位不可用')
    return
  }
  selectedSeat.value = seat
}

const getSeatStatusType = (status) => {
  const types = {
    available: 'success',
    reserved: 'warning',
    occupied: 'danger'
  }
  return types[status] || 'info'
}

const formatTime = (time) => {
  return time ? dayjs(time).format('HH:mm') : ''
}

const calculatePrice = () => {
  if (!selectedRoom.value || !duration.value) return 0
  return ((duration.value / 60) * selectedRoom.value.pricePerHour).toFixed(2)
}

// 检查预约冲突
const checkReservationConflict = async () => {
  if (!form.value.roomId || !form.value.date || !form.value.startTime || !form.value.endTime) {
    return false
  }

  try {
    const startTime = `${form.value.date}T${formatTime(form.value.startTime)}:00`
    const endTime = `${form.value.date}T${formatTime(form.value.endTime)}:00`
    
    // 获取该座位当天的所有预约记录
    const dayStart = `${form.value.date}T00:00:00`
    const dayEnd = `${form.value.date}T23:59:59`
    const reservations = await reservationApi.getReservationsByTimeRange(dayStart, dayEnd)
    
    // 检查是否有冲突的预约，只考虑状态为PENDING的预约
    const hasConflict = reservations.some(reservation => {
      // 检查是否是同一个座位且状态为PENDING
      if (reservation.seatId === selectedSeat.value.id && reservation.status === 'PENDING') {
        // 检查时间是否重叠
        const reservationStart = dayjs(reservation.startTime)
        const reservationEnd = dayjs(reservation.endTime)
        const newStart = dayjs(startTime)
        const newEnd = dayjs(endTime)
        
        // 检查时间是否重叠
        const isOverlapping = (
          // 新预约的开始时间在已有预约的时间段内
          (newStart.isAfter(reservationStart) && newStart.isBefore(reservationEnd)) ||
          // 新预约的结束时间在已有预约的时间段内
          (newEnd.isAfter(reservationStart) && newEnd.isBefore(reservationEnd)) ||
          // 新预约完全包含已有预约
          (newStart.isBefore(reservationStart) && newEnd.isAfter(reservationEnd)) ||
          // 新预约与已有预约的开始时间相同
          newStart.isSame(reservationStart) ||
          // 新预约与已有预约的结束时间相同
          newEnd.isSame(reservationEnd) ||
          // 新预约完全被已有预约包含
          (newStart.isAfter(reservationStart) && newEnd.isBefore(reservationEnd))
        )
        
        console.log(`检查座位 ${selectedSeat.value.number} 的时间冲突:`, {
          reservationTime: `${reservationStart.format('HH:mm')} - ${reservationEnd.format('HH:mm')}`,
          selectedTime: `${newStart.format('HH:mm')} - ${newEnd.format('HH:mm')}`,
          isOverlapping
        })
        
        return isOverlapping
      }
      return false
    })
    
    if (hasConflict) {
      ElMessage.warning('所选时间段已被预约，请选择其他时间')
      return true
    }
    
    return false
  } catch (error) {
    console.error('检查预约冲突失败:', error)
    ElMessage.error('检查预约冲突失败，请稍后重试')
    return true
  }
}

const submitReservation = async () => {
  if (!form.value.roomId || !form.value.date || !form.value.startTime || 
      !form.value.endTime || !selectedSeat.value) {
    ElMessage.warning('请填写完整的预约信息')
    return
  }

  try {
    loading.value = true
    
    // 检查预约冲突
    const hasConflict = await checkReservationConflict()
    if (hasConflict) {
      return
    }
    
    // 构造预约数据
    const reservationData = {
      roomId: form.value.roomId,
      seatId: selectedSeat.value.id,
      userId: JSON.parse(localStorage.getItem('userInfo')).id,
      date: form.value.date,
      startTime: `${form.value.date}T${formatTime(form.value.startTime)}:00`,
      endTime: `${form.value.date}T${formatTime(form.value.endTime)}:00`,
      duration: duration.value,
      totalPrice: calculatePrice(),
      status: 'PENDING'
    }
    
    console.log('提交预约数据:', reservationData)
    
    // 调用预约API
    const response = await reservationApi.createReservation(reservationData)
    console.log('预约响应:', response)
    
    if (response) {
      // 注意：根据需求，预约成功后不修改座位表中的座位状态
      // 座位的可用状态将通过 fetchAvailableSeats 函数在查询时动态计算
      
      ElMessage.success('预约成功')
      // 重置表单
      form.value = {
        roomId: '',
        date: '',
        startTime: null,
        endTime: null
      }
      selectedSeat.value = null
      // 刷新可用座位列表
      await fetchAvailableSeats()
    } else {
      throw new Error('预约失败')
    }
  } catch (error) {
    console.error('预约失败:', error)
    ElMessage.error(error.response?.data?.message || '预约失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!checkLoginStatus()) return
  fetchRooms()
})
</script>

<style scoped>
.reservations {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
}

.reservation-form {
  max-width: 1200px;
  margin: 0 auto;
}

.seat-selection {
  margin-top: 30px;
}

.seat-selection h3 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
  width: 100%;
}

.seat-card {
  cursor: pointer;
  transition: all 0.3s;
  padding: 12px;
  width: 100%;
  box-sizing: border-box;
}

.seat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.seat-card.selected {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.seat-card.reserved {
  cursor: not-allowed;
  opacity: 0.7;
  background-color: #fdf6ec;
}

.seat-card.reserved:hover {
  transform: none;
  box-shadow: none;
}

.seat-number {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 10px;
}

.seat-status {
  text-align: center;
}

.reservation-summary {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.reservation-summary h3 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.form-actions {
  margin-top: 20px;
  text-align: center;
}

:deep(.el-descriptions__label) {
  width: 120px;
  color: #606266;
}

:deep(.el-descriptions__content) {
  color: #303133;
}

.reservation-times {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}

.time-slot {
  padding: 2px 4px;
  background: #f5f5f5;
  border-radius: 4px;
  margin: 2px 0;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .reservations {
    padding: 12px;
  }

  .card-header h2 {
    font-size: 16px;
  }

  .reservation-form {
    padding: 0;
    width: 100%;
  }

  /* 表单布局优化 */
  :deep(.el-form-item) {
    margin-bottom: 16px;
    width: 100%;
  }

  :deep(.el-form-item__label) {
    float: none;
    display: block;
    text-align: left;
    padding: 0 0 8px;
    font-size: 14px;
    font-weight: 500;
    line-height: 1.4;
  }

  :deep(.el-form-item__content) {
    margin-left: 0 !important;
    line-height: 1.4;
    width: 100%;
  }

  :deep(.el-select),
  :deep(.el-date-picker),
  :deep(.el-time-picker) {
    width: 100% !important;
  }

  :deep(.el-input__wrapper) {
    padding: 8px 12px;
    width: 100%;
    box-sizing: border-box;
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    width: 100%;
  }

  /* 座位选择区域优化 */
  .seat-selection {
    margin-top: 20px;
    width: 100%;
  }

  .seat-selection h3 {
    font-size: 16px;
    margin-bottom: 16px;
  }

  .seat-grid {
    grid-template-columns: repeat(3, 1fr) !important;
    gap: 8px !important;
    width: 100% !important;
    box-sizing: border-box;
    padding: 0 !important;
    margin-bottom: 16px !important;
  }

  .seat-card {
    width: 100% !important;
    padding: 8px !important;
    margin: 0 !important;
    box-sizing: border-box;
  }

  .seat-card :deep(.el-card__body) {
    padding: 8px !important;
    width: 100% !important;
    box-sizing: border-box;
  }

  .seat-number {
    font-size: 16px !important;
    margin-bottom: 4px !important;
  }

  .seat-status :deep(.el-tag) {
    padding: 0 4px !important;
    font-size: 11px !important;
    height: auto !important;
    line-height: 1.5 !important;
  }

  /* 预约信息确认区域优化 */
  .reservation-summary {
    margin-top: 20px;
    padding: 16px;
    width: 100%;
    box-sizing: border-box;
  }

  .reservation-summary h3 {
    font-size: 16px;
    margin-bottom: 16px;
  }

  :deep(.el-descriptions) {
    font-size: 14px;
    width: 100%;
  }

  :deep(.el-descriptions__label) {
    width: 90px;
    font-size: 13px;
    padding: 8px;
    line-height: 1.4;
  }

  :deep(.el-descriptions__content) {
    font-size: 13px;
    padding: 8px;
    line-height: 1.4;
    word-break: break-word;
  }

  .price {
    font-size: 16px;
  }

  /* 按钮优化 */
  .form-actions {
    margin-top: 16px;
    width: 100%;
  }

  :deep(.el-button) {
    width: 100%;
    margin: 0;
    padding: 10px 16px;
    font-size: 14px;
  }

  /* 行列布局优化 */
  :deep(.el-row) {
    margin-left: 0 !important;
    margin-right: 0 !important;
    width: 100%;
  }

  :deep(.el-col) {
    padding-left: 0 !important;
    padding-right: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    flex: 0 0 100% !important;
  }

  /* 时间选择器优化 */
  :deep(.el-date-editor.el-input),
  :deep(.el-date-editor.el-input__wrapper),
  :deep(.el-time-picker),
  :deep(.el-time-picker__editor-wrap) {
    width: 100% !important;
  }

  :deep(.el-date-picker),
  :deep(.el-time-panel) {
    width: 280px !important;
  }

  /* 描述列表优化 */
  :deep(.el-descriptions__body) {
    background-color: #fff;
  }

  :deep(.el-descriptions__table) {
    width: 100%;
    table-layout: fixed;
  }

  :deep(.el-descriptions__cell) {
    padding: 8px;
  }

  /* 修复时间选择器和下拉菜单在移动端的显示问题 */
  :deep(.el-select__popper),
  :deep(.el-picker__popper) {
    width: 80vw !important;
    max-width: 300px;
  }

  /* 修复日期和时间选择器在移动端的显示 */
  :deep(.el-date-picker),
  :deep(.el-time-picker) {
    --el-date-editor-width: 100%;
  }

  /* 修复卡片容器宽度问题 */
  :deep(.el-card) {
    width: 100% !important;
    margin: 0 !important;
    box-sizing: border-box !important;
  }
  
  :deep(.el-card__body) {
    width: 100% !important;
    box-sizing: border-box;
    padding: 16px !important;
  }
}

/* 平板设备适配 */
@media screen and (min-width: 769px) and (max-width: 1024px) {
  .reservations {
    padding: 16px;
  }

  .seat-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }

  :deep(.el-form-item__label) {
    font-size: 14px;
  }

  :deep(.el-select),
  :deep(.el-date-picker),
  :deep(.el-time-picker) {
    width: 100% !important;
  }

  /* 平板设备表单布局优化 */
  :deep(.el-row) {
    flex-wrap: wrap;
  }

  :deep(.el-col) {
    width: 50% !important;
    max-width: 50% !important;
    flex: 0 0 50% !important;
  }
}

/* 表单元素样式优化 */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color);
}

:deep(.el-select .el-input__wrapper) {
  cursor: pointer;
}

:deep(.el-date-editor.el-input),
:deep(.el-date-editor.el-input__inner) {
  width: 100%;
}

/* 时间选择器样式优化 */
:deep(.el-time-picker__editor-wrap) {
  padding: 0;
}

:deep(.el-time-picker__editor) {
  padding: 0 8px;
}

/* 描述列表样式优化 */
:deep(.el-descriptions__body) {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-descriptions__table) {
  width: 100%;
}

:deep(.el-descriptions__cell) {
  padding: 12px;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  word-break: break-all;
}
</style>