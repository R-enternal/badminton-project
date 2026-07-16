<template>
  <div class="page">
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" @click="handleAdd">新增课程</el-button>
    </div>

    <el-form inline class="search-form">
      <el-form-item label="教练">
        <el-select v-model="selectedCoachId" placeholder="选择教练" @change="fetchList" clearable>
          <el-option
            v-for="coach in coaches"
            :key="coach.id"
            :label="coach.name"
            :value="coach.id"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="coachName" label="教练" />
      <el-table-column prop="name" label="课程名称" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="durationMinutes" label="时长（分钟）" />
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="教练">
          <el-select v-model="form.coachId" placeholder="选择教练">
            <el-option
              v-for="coach in coaches"
              :key="coach.id"
              :label="coach.name"
              :value="coach.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如：成人/青少年/1对1/团体" />
        </el-form-item>
        <el-form-item label="时长（分钟）">
          <el-input-number v-model="form.durationMinutes" :min="1" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="课程说明">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { coachApi } from '../../api/coach'

const list = ref([])
const coaches = ref([])
const loading = ref(false)
const selectedCoachId = ref('')
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  coachId: '',
  name: '',
  category: '',
  durationMinutes: 60,
  price: 0,
  description: '',
  status: 1
})

const fetchCoaches = async () => {
  coaches.value = await coachApi.list()
}

const fetchList = async () => {
  if (!selectedCoachId.value) {
    list.value = []
    return
  }
  loading.value = true
  try {
    list.value = await coachApi.listCourses(selectedCoachId.value)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    coachId: selectedCoachId.value || '',
    name: '',
    category: '',
    durationMinutes: 60,
    price: 0,
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.coachId || !form.name || !form.category) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    if (form.id) {
      await coachApi.updateCourse(form.id, form)
    } else {
      await coachApi.createCourse(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该课程吗？', '提示', { type: 'warning' })
    .then(async () => {
      await coachApi.deleteCourse(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

onMounted(async () => {
  await fetchCoaches()
  if (coaches.value.length > 0) {
    selectedCoachId.value = coaches.value[0].id
    fetchList()
  }
})
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

.search-form {
  margin-bottom: 20px;
}
</style>
