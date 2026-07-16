<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <template #header>场地总数</template>
          <div class="stat-num">{{ stats.venueCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>教练总数</template>
          <div class="stat-num">{{ stats.coachCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>商品总数</template>
          <div class="stat-num">{{ stats.productCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>待处理订单</template>
          <div class="stat-num">{{ stats.pendingOrderCount }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { venueApi } from '../api/venue'
import { coachApi } from '../api/coach'
import { shopApi } from '../api/shop'

const stats = reactive({
  venueCount: 0,
  coachCount: 0,
  productCount: 0,
  pendingOrderCount: 0
})

onMounted(async () => {
  try {
    const venues = await venueApi.list()
    const coaches = await coachApi.list()
    const products = await shopApi.listProducts({})
    const orders = await shopApi.listOrders({ status: 1 })
    stats.venueCount = venues.length
    stats.coachCount = coaches.length
    stats.productCount = products.length
    stats.pendingOrderCount = orders.length
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.stat-num {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  text-align: center;
}
</style>
