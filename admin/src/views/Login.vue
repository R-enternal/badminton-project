<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <template #header>
        <div class="login-title">羽毛球馆管理系统</div>
      </template>
      <el-form :model="form" label-width="0">
        <el-form-item>
          <el-input
            v-model="form.code"
            placeholder="请输入任意登录码"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  code: 'admin'
})

const handleLogin = async () => {
  if (!form.code) {
    ElMessage.warning('请输入登录码')
    return
  }
  loading.value = true
  try {
    const res = await userApi.login(form.code)
    userStore.setToken(res.token)
    userStore.setUserInfo(res.userInfo)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
}

.login-card {
  width: 400px;
}

.login-title {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}
</style>
