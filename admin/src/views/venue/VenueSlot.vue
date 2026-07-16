<template>
  <div class="page">
    <div class="page-header">
      <h2>场地时段管理</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          @change="fetchList"
        />
        <el-button type="primary" @click="handleGenerate">生成时段</el-button>
      </div>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="venueId" label="场地ID" width="80" />
      <el-table-column prop="bookingDate" label="日期" />
      <el-table-column prop="startTime" label="开始时间" />
      <el-table-column prop="endTime" label="结束时间" />
      <el-table-column prop="price" label="售价" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { venueApi } from '../../api/venue'

const list = ref([])
const loading = ref(false)
const selectedDate = ref(new Date().toISOString().split('T')[0])

const fetchList = async () => {
  if (!selectedDate.value) return
  loading.value = true
  try {
    list.value = await venueApi.listSlots({ date: selectedDate.value })
  } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  if (!selectedDate.value) {
    ElMessage.warning('请选择日期')
    return
  }
  try {
    await venueApi.generateSlots(selectedDate.value)
    ElMessage.success('生成成功')
    fetchList()
  } catch (e) {
    console.error(e)
  }
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '关闭', 1: '可约', 2: '已约' }
  return map[status] || '未知'
}

onMounted(fetchList)
</script>

<style scoped>
.page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 12px;
}
</style>
