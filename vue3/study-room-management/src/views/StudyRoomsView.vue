<template>
  <div class="study-rooms">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>自习室管理</h2>
          <el-button v-if="isAdmin" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加自习室
          </el-button>
        </div>
      </template>

      <!-- 自习室列表 -->
      <el-table :data="rooms" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="name" label="自习室名称" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="200" fixed="right" v-if="isAdmin">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              v-if="row.status !== 'in_use'">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑自习室对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加自习室' : '编辑自习室'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="自习室名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入自习室名称" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="开放" value="OPEN" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { studyRoomApi } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const formRef = ref(null)

const form = reactive({
  name: '',
  capacity: 1,
  pricePerHour: 0,
  status: 'OPEN',
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入自习室名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入座位数', trigger: 'blur' }
  ],
  pricePerHour: [
    { required: true, message: '请输入每小时价格', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const rooms = ref([])

const getStatusType = (status) => {
  const types = {
    'OPEN': 'success',
    'MAINTENANCE': 'warning',
    'CLOSED': 'danger'
  }
  return types[status] || 'info'
}

const isAdmin = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  return userInfo?.role === 'ADMIN'
})

// 获取自习室列表
const fetchRooms = async () => {
  try {
    loading.value = true
    const data = await studyRoomApi.getAllRooms()
    console.log('获取到的自习室数据:', data)
    rooms.value = data.map(room => {
      const statusMap = {
        'OPEN': '开放',
        'MAINTENANCE': '维护中',
        'CLOSED': '关闭'
      }
      return {
        ...room,
        name: room.roomName,
        statusText: statusMap[room.status] || '未知'
      }
    })
  } catch (error) {
    console.error('获取自习室列表失败:', error)
    ElMessage.error('获取自习室列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.name = ''
  form.capacity = 1
  form.pricePerHour = 0
  form.status = 'OPEN'
  form.description = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该自习室？', '提示', {
      type: 'warning'
    })
    await studyRoomApi.deleteRoom(row.id)
    ElMessage.success('删除成功')
    fetchRooms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除自习室失败:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const submitData = {
      ...form,
      roomName: form.name
    }
    
    if (dialogType.value === 'add') {
      const response = await studyRoomApi.createRoom(submitData)
      console.log('创建自习室响应:', response)
      ElMessage.success('添加成功')
    } else {
      const response = await studyRoomApi.updateRoom(form.id, submitData)
      console.log('更新自习室响应:', response)
      ElMessage.success('修改成功')
    }
    
    dialogVisible.value = false
    fetchRooms() // 刷新列表
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchRooms()
  
  // 为表格添加响应式支持
  setTimeout(() => {
    const tableRows = document.querySelectorAll('.el-table__row')
    tableRows.forEach(row => {
      const cells = row.querySelectorAll('.el-table__cell')
      if (cells.length > 0) {
        // 设置单元格的data-label属性
        const labels = ['编号', '自习室名称', '状态', '描述', '操作']
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
.study-rooms {
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

:deep(.el-button) {
  padding: 8px 16px;
  font-weight: 500;
}

:deep(.el-button--small) {
  padding: 6px 12px;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary);
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .study-rooms {
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
    margin-left: 8px;
    margin-right: 0;
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

  /* 移动端按钮优化 */
  :deep(.el-button--small) {
    padding: 8px 12px;
    font-size: 12px;
  }

  /* 移动端对话框优化 */
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto;
  }

  :deep(.el-card__body) {
    padding: 12px !important;
    width: 100% !important;
    box-sizing: border-box;
  }
  
  /* 确保单元格内容显示标签 */
  :deep(.el-table__row .el-table__cell:nth-child(1) .cell::before) {
    content: "编号: ";
  }

  :deep(.el-table__row .el-table__cell:nth-child(2) .cell::before) {
    content: "自习室名称: ";
  }

  :deep(.el-table__row .el-table__cell:nth-child(3) .cell::before) {
    content: "状态: ";
  }

  :deep(.el-table__row .el-table__cell:nth-child(4) .cell::before) {
    content: "描述: ";
  }
  
  /* 修复卡片不占满宽度的问题 */
  :deep(.el-card) {
    width: 100% !important;
    margin: 0 !important;
    box-sizing: border-box !important;
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
}

/* 平板设备适配 */
@media screen and (min-width: 769px) and (max-width: 1024px) {
  .study-rooms {
    padding: 16px;
  }

  :deep(.el-table) {
    font-size: 14px;
  }

  :deep(.el-dialog) {
    width: 70% !important;
  }
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

/* 确保表格内容能够正确显示 */
:deep(.el-table__cell:last-child .cell)::before {
  display: none;
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