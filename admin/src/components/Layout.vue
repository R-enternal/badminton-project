<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <div class="logo">羽毛球馆管理系统</div>
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-sub-menu index="/venue">
          <template #title>
            <el-icon><Basketball /></el-icon>
            <span>场地预约</span>
          </template>
          <el-menu-item index="/venue">场地管理</el-menu-item>
          <el-menu-item index="/venue-slot">时段管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/coach">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>教练课程</span>
          </template>
          <el-menu-item index="/coach">教练管理</el-menu-item>
          <el-menu-item index="/coach-course">课程管理</el-menu-item>
          <el-menu-item index="/coach-schedule">排班管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/product">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品商城</span>
          </template>
          <el-menu-item index="/category">商品分类</el-menu-item>
          <el-menu-item index="/product">商品管理</el-menu-item>
          <el-menu-item index="/order">订单管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header height="60px" class="header">
        <div class="header-right">
          <span class="username">{{ userStore.userInfo.nickname || '管理员' }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background-color: #2b3649;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  color: #606266;
}
</style>
