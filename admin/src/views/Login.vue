<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <template #header>
        <div class="login-title">🏸 羽毛球馆管理系统</div>
        <div class="login-subtitle">管理员登录</div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入管理员手机号"
            size="large"
            maxlength="11"
          >
            <template #prefix>📱</template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>🔒</template>
          </el-input>
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
import { passwordRule, phoneRule } from '../utils/validate'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()

const form = reactive({
  phone: '',
  password: ''
})

const rules = {
  phone: [phoneRule],
  password: [passwordRule]
}

const handleLogin = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userApi.passwordLogin(form.phone, form.password)

    // 校验角色权限，只有 ADMIN 能进入管理后台
    if (res.userInfo.role !== 'ADMIN') {
      ElMessage.error('当前账号无管理员权限')
      return
    }

    userStore.setToken(res.token)
    userStore.setUserInfo(res.userInfo)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (err) {
    ElMessage.error(err.message || '登录失败')
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
  background: linear-gradient(135deg, #fffaf5 0%, #ffedd5 100%);
}

.login-card {
  width: 420px;
  border-radius: 20px;
}

.login-title {
  text-align: center;
  font-size: 22px;
  font-weight: 800;
  color: #451a03;
}

.login-subtitle {
  text-align: center;
  font-size: 14px;
  color: #92400e;
  margin-top: 8px;
}
</style>
