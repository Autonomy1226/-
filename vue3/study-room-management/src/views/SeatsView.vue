<template>
  <div class="seats">
    <h2>座位管理</h2>
    <div class="seat-controls">
      <select v-model="selectedRoom" class="room-select">
        <option value="">选择自习室</option>
        <option v-for="room in rooms" :key="room.id" :value="room.id">
          {{ room.name }}
        </option>
      </select>
      <button class="btn-refresh" @click="handleRefresh">刷新状态</button>
      <button class="btn-add" @click="openAddDialog" v-if="selectedRoom">添加座位</button>
    </div>
    
    <div class="seat-grid" v-if="selectedRoom">
      <div v-for="seat in seats" :key="seat.id" 
           :class="['seat', seat.status]"
           @click="openSeatDialog(seat)">
        <span class="seat-number">{{ seat.number }}</span>
        <span class="seat-status">{{ seat.statusText }}</span>
      </div>
    </div>

    <!-- 座位状态管理对话框 -->
    <div class="dialog" v-if="showDialog">
      <div class="dialog-content">
        <h3>座位状态管理</h3>
        <div class="seat-info">
          <p>座位号：{{ selectedSeat?.number }}</p>
          <p>当前状态：{{ selectedSeat?.statusText }}</p>
        </div>
        <div class="status-actions">
          <button 
            v-for="status in seatStatuses" 
            :key="status.value"
            :class="['btn-status', { active: selectedSeat?.status === status.value }]"
            @click="updateSeatStatus(status.value)">
            {{ status.label }}
          </button>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-confirm" @click="confirmStatusChange">确认</button>
        </div>
      </div>
    </div>

    <!-- 添加座位对话框 -->
    <div class="dialog" v-if="showAddDialog">
      <div class="dialog-content">
        <h3>添加座位</h3>
        <div class="add-seat-form">
          <div class="form-group">
            <label>座位号</label>
            <input 
              type="text" 
              v-model="newSeat.seatNumber" 
              placeholder="请输入座位号"
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label>价格（元/小时）</label>
            <input 
              type="number" 
              v-model="newSeat.price" 
              placeholder="请输入价格"
              class="form-control"
              min="0"
              step="0.1"
            />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeAddDialog">取消</button>
          <button class="btn-confirm" @click="confirmAddSeat" :disabled="!isValidNewSeat">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { seatApi, studyRoomApi } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()
const selectedRoom = ref('')
const showDialog = ref(false)
const selectedSeat = ref(null)
const loading = ref(false)

const rooms = ref([])
const seats = ref([])

const seatStatuses = [
  { label: '可用', value: 'AVAILABLE' },
  { label: '不可用', value: 'UNAVAILABLE' }
]

const isAdmin = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  return userInfo?.role === 'ADMIN'
})

const getStatusType = (status) => {
  const types = {
    'AVAILABLE': 'success',
    'UNAVAILABLE': 'danger'
  }
  return types[status] || 'info'
}

// 获取自习室列表
const fetchRooms = async () => {
  try {
    const data = await studyRoomApi.getAllRooms()
    console.log('获取到的自习室数据:', data)
    rooms.value = data.map(room => ({
      id: room.id,
      name: room.roomName // 使用 roomName 作为显示名称
    }))
  } catch (error) {
    console.error('获取自习室列表失败:', error)
    ElMessage.error('获取自习室列表失败')
  }
}

// 获取座位列表
const fetchSeats = async () => {
  if (!selectedRoom.value) return
  
  try {
    loading.value = true
    const data = await seatApi.getSeatsByRoom(selectedRoom.value)
    console.log('获取到的座位数据:', data)
    seats.value = data.map(seat => {
      const status = seatStatuses.find(s => s.value === seat.status)
      return {
        ...seat,
        number: seat.seatNumber,
        statusText: status ? status.label : '未知'
      }
    })
  } catch (error) {
    console.error('获取座位列表失败:', error)
    ElMessage.error('获取座位列表失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  if (selectedRoom.value) {
    fetchSeats()
  }
}

const openSeatDialog = (seat) => {
  if (!isAdmin.value) {
    ElMessage.warning('需要管理员权限')
    return
  }
  selectedSeat.value = { ...seat }
  showDialog.value = true
}

const closeDialog = () => {
  showDialog.value = false
  selectedSeat.value = null
}

const updateSeatStatus = (status) => {
  if (selectedSeat.value) {
    selectedSeat.value.status = status
    const statusInfo = seatStatuses.find(s => s.value === status)
    selectedSeat.value.statusText = statusInfo ? statusInfo.label : '未知'
  }
}

const confirmStatusChange = async () => {
  if (!isAdmin.value) {
    ElMessage.warning('需要管理员权限')
    return
  }
  try {
    await seatApi.updateSeatStatus(selectedSeat.value.id, selectedSeat.value.status)
    ElMessage.success('更新成功')
    fetchSeats()
    closeDialog()
  } catch (error) {
    console.error('更新座位状态失败:', error)
    ElMessage.error('更新座位状态失败')
  }
}

// 添加座位相关的响应式数据
const showAddDialog = ref(false)
const newSeat = ref({
  seatNumber: '',
  price: 0
})

// 验证新座位数据是否有效
const isValidNewSeat = computed(() => {
  return newSeat.value.seatNumber && 
         newSeat.value.price > 0 && 
         !seats.value.some(seat => seat.seatNumber === newSeat.value.seatNumber)
})

// 打开添加座位对话框
const openAddDialog = () => {
  if (!isAdmin.value) {
    ElMessage.warning('需要管理员权限')
    return
  }
  newSeat.value = {
    seatNumber: '',
    price: 0
  }
  showAddDialog.value = true
}

// 关闭添加座位对话框
const closeAddDialog = () => {
  showAddDialog.value = false
  newSeat.value = {
    seatNumber: '',
    price: 0
  }
}

// 确认添加座位
const confirmAddSeat = async () => {
  if (!isValidNewSeat.value) {
    ElMessage.warning('请填写有效的座位信息')
    return
  }

  try {
    const seatData = {
      roomId: selectedRoom.value,
      seatNumber: newSeat.value.seatNumber,
      price: newSeat.value.price,
      status: 'AVAILABLE'
    }

    await seatApi.createSeat(seatData)
    ElMessage.success('添加座位成功')
    closeAddDialog()
    fetchSeats() // 刷新座位列表
  } catch (error) {
    console.error('添加座位失败:', error)
    ElMessage.error('添加座位失败')
  }
}

// 监听自习室选择变化
watch(selectedRoom, () => {
  fetchSeats()
})

onMounted(() => {
  fetchRooms()
})
</script>

<style scoped>
.seats {
  padding: 20px;
}

.seat-controls {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.room-select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 200px;
}

.btn-refresh {
  padding: 8px 16px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-add {
  padding: 8px 16px;
  background: #2ecc71;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.seat {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s;
}

.seat:hover {
  transform: scale(1.05);
}

.seat.available {
  background: #e1f7e1;
  color: #2ecc71;
}

.seat.unavailable {
  background: #fde2e2;
  color: #e74c3c;
}

.seat-number {
  font-size: 18px;
  font-weight: bold;
}

.seat-status {
  font-size: 12px;
  margin-top: 5px;
}

.dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  min-width: 300px;
}

.seat-info {
  margin: 15px 0;
}

.status-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin: 15px 0;
}

.btn-status {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
}

.btn-status.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel, .btn-confirm {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-cancel {
  background: #e2e3e5;
  color: #666;
}

.btn-confirm {
  background: #3498db;
  color: white;
}

.add-seat-form {
  margin: 20px 0;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #666;
}

.form-control {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-control:focus {
  border-color: #3498db;
  outline: none;
}
</style> 