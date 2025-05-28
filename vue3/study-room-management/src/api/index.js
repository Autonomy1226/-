import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080',  // 移除 /api 前缀
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // 允许跨域请求携带cookie
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 添加跨域请求头
    config.headers['Access-Control-Allow-Origin'] = 'http://localhost:5173'
    console.log('Request:', config)
    return config
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    console.log('Response:', response)
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    console.error('Response Error:', error)
    if (error.response) {
      // 服务器返回错误状态码
      console.error('Error Status:', error.response.status)
      console.error('Error Data:', error.response.data)
      ElMessage.error(error.response.data?.message || `请求失败 (${error.response.status})`)
    } else if (error.request) {
      // 请求发出但没有收到响应
      console.error('No Response:', error.request)
      ElMessage.error('无法连接到服务器，请检查网络连接')
    } else {
      // 请求配置出错
      console.error('Request Config Error:', error.message)
      ElMessage.error(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

// 用户相关API
export const userApi = {
  // 注册
  register: (data) => api.post('/api/users/register', {
    username: data.username,
    password: data.password,
    email: data.email,
    phone: data.phone,
    role: 'USER'  // 默认注册为普通用户
  }),
  // 登录
  login: (data) => {
    const formData = new URLSearchParams()
    formData.append('username', data.username)
    formData.append('password', data.password)
    return api.post('/api/users/login', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
  },
  // 获取用户信息
  getUserById: (id) => api.get(`/api/users/${id}`),
  getUserByUsername: (username) => api.get(`/api/users/username/${username}`),
  getUsersByRole: (role) => api.get(`/api/users/role/${role}`),
  getAllUsers: () => api.get('/api/users'),
  // 更新用户信息
  updateUser: (id, data) => api.put(`/api/users/${id}`, data),
  updateUserStatus: (id, status) => api.put(`/api/users/${id}/status`, { status }),
  // 删除用户
  deleteUser: (id) => api.delete(`/api/users/${id}`)
}

// 自习室相关API
export const studyRoomApi = {
  // 创建自习室
  createRoom: (data) => api.post('/api/study-rooms', data),
  // 获取自习室信息
  getRoomById: (id) => api.get(`/api/study-rooms/${id}`),
  getRoomsByStatus: (status) => api.get(`/api/study-rooms/status/${status}`),
  getAllRooms: () => api.get('/api/study-rooms'),
  // 更新自习室信息
  updateRoom: (id, data) => api.put(`/api/study-rooms/${id}`, data),
  updateRoomStatus: (id, status) => api.put(`/api/study-rooms/${id}/status`, { status }),
  // 删除自习室
  deleteRoom: (id) => api.delete(`/api/study-rooms/${id}`)
}

// 座位相关API
export const seatApi = {
  // 创建座位
  createSeat: (data) => api.post('/api/seats', data),
  // 获取座位信息
  getSeatById: (id) => api.get(`/api/seats/${id}`),
  getSeatsByRoom: (roomId) => api.get(`/api/seats/room/${roomId}`),
  getSeatsByStatus: (status) => api.get(`/api/seats/status/${status}`),
  getSeatsByRoomAndStatus: (roomId, status) => api.get(`/api/seats/room/${roomId}/status/${status}`),
  getAllSeats: () => api.get('/api/seats'),
  // 更新座位信息
  updateSeat: (id, data) => api.put(`/api/seats/${id}`, data),
  updateSeatStatus: (id, status) => api.put(`/api/seats/${id}/status?status=${status}`),
  // 删除座位
  deleteSeat: (id) => api.delete(`/api/seats/${id}`)
}

// 预约相关API
export const reservationApi = {
  // 创建预约
  createReservation: (data) => api.post('/api/reservations', data),
  // 获取预约信息
  getReservationById: (id) => api.get(`/api/reservations/${id}`),
  getReservationsByUser: (userId) => api.get(`/api/reservations/user/${userId}`),
  getReservationsBySeat: (seatId) => api.get(`/api/reservations/seat/${seatId}`),
  getReservationsByStatus: (status) => api.get(`/api/reservations/status/${status}`),
  getReservationsByTimeRange: (startTime, endTime) => api.get('/api/reservations/time-range', {
    params: { startTime, endTime }
  }),
  getAllReservations: () => api.get('/api/reservations'),
  // 更新预约信息
  updateReservation: (id, data) => api.put(`/api/reservations/${id}`, data),
  updateReservationStatus: (id, status) => api.put(`/api/reservations/${id}/status`, { status }),
  // 取消预约
  cancelReservation: (id) => api.post(`/api/reservations/${id}/cancel`),
  // 删除预约
  deleteReservation: (id) => api.delete(`/api/reservations/${id}`)
}

// 使用记录相关API
export const usageRecordApi = {
  // 创建使用记录
  createUsageRecord: (data) => api.post('/api/usage-records', data),
  // 获取使用记录
  getUsageRecordById: (id) => api.get(`/api/usage-records/${id}`),
  getUsageRecordsByReservation: (reservationId) => api.get(`/api/usage-records/reservation/${reservationId}`),
  getUsageRecordsByStatus: (status) => api.get(`/api/usage-records/status/${status}`),
  getUsageRecordsByTimeRange: (startTime, endTime) => api.get('/api/usage-records/time-range', {
    params: { startTime, endTime }
  }),
  getAllUsageRecords: () => api.get('/api/usage-records'),
  // 更新使用记录
  updateUsageRecord: (id, data) => api.put(`/api/usage-records/${id}`, data),
  updateUsageRecordStatus: (id, status) => api.put(`/api/usage-records/${id}/status`, { status }),
  // 删除使用记录
  deleteUsageRecord: (id) => api.delete(`/api/usage-records/${id}`)
}

export default {
  userApi,
  studyRoomApi,
  seatApi,
  reservationApi,
  usageRecordApi
} 