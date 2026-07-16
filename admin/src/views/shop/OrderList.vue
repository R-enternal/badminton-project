<template>
  <div class="page">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <el-form inline class="search-form">
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部状态" @change="fetchList" clearable>
          <el-option label="待付款" :value="0" />
          <el-option label="已付款" :value="1" />
          <el-option label="已发货" :value="2" />
          <el-option label="已收货" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchList">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="totalAmount" label="总金额" />
      <el-table-column prop="receiverName" label="收货人" />
      <el-table-column prop="receiverPhone" label="收货电话" />
      <el-table-column prop="receiverAddress" label="收货地址" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1"
            type="success"
            size="small"
            @click="handleShip(row)"
          >
            发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shopApi } from '../../api/shop'

const list = ref([])
const loading = ref(false)
const query = reactive({
  status: ''
})

const fetchList = async () => {
  loading.value = true
  try {
    const params = {}
    if (query.status !== '') params.status = query.status
    list.value = await shopApi.listOrders(params)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: '', 4: 'success', 5: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已收货', 4: '已完成', 5: '已取消' }
  return map[status] || '未知'
}

const handleShip = (row) => {
  ElMessageBox.confirm(`确认发货订单 ${row.orderNo} 吗？`, '发货确认', { type: 'info' })
    .then(async () => {
      await shopApi.shipOrder(row.id)
      ElMessage.success('发货成功')
      fetchList()
    })
    .catch(() => {})
}

onMounted(fetchList)
</script>

<style scoped>
.page { padding: 20px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.search-form { margin-bottom: 20px; }
</style>
