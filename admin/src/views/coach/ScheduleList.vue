<template>
  <div class="page">
    <div class="page-header">
      <h2>排班管理</h2>
      <el-button type="primary" @click="handleAdd">新增排班</el-button>
    </div>

    <el-form inline class="search-form">
      <el-form-item label="教练">
        <el-select v-model="query.coachId" placeholder="选择教练" @change="fetchList" clearable>
          <el-option
            v-for="coach in coaches"
            :key="coach.id"
            :label="coach.name"
            :value="coach.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker
          v-model="query.workDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          @change="fetchList"
        />
      </el-form-item>
    </el-form>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="coachName" label="教练" />
      <el-table-column prop="workDate" label="日期" />
      <el-table-column prop="startTime" label="开始时间" />
      <el-table-column prop="endTime" label="结束时间" />
      <el-table-column prop="courseName" label="关联课程" />
      <el-table-column prop="isBooked" label="预约状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isBooked === 1 ? 'danger' : 'success'">
            {{ row.isBooked === 1 ? '已约' : '可约' }}
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑排班' : '新增排班'" width="500px">
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
        <el-form-item label="日期">
          <el-date-picker v-model="form.workDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker v-model="form.startTime" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker v-model="form.endTime" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="关联课程">
          <el-select v-model="form.courseId" placeholder="选择课程（可选）" clearable>
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
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
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { coachApi } from '../../api/coach'

const list = ref([])
const coaches = ref([])
const courses = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const query = reactive({
  coachId: '',
  workDate: new Date().toISOString().split('T')[0]
})
const form = reactive({
  id: null,
  coachId: '',
  workDate: '',
  startTime: '',
  endTime: '',
  courseId: null
})

const fetchCoaches = async () => {
  coaches.value = await coachApi.list()
}

const fetchCourses = async () => {
  if (!form.coachId) {
    courses.value = []
    return
  }
  courses.value = await coachApi.listCourses(form.coachId)
}

const fetchList = async () => {
  if (!query.coachId || !query.workDate) return
  loading.value = true
  try {
    list.value = await coachApi.listSchedule({
      coachId: query.coachId,
      date: query.workDate
    })
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    coachId: query.coachId || '',
    workDate: query.workDate,
    startTime: '09:00:00',
    endTime: '10:00:00',
    courseId: null
  })
  fetchCourses()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  fetchCourses()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.coachId || !form.workDate || !form.startTime || !form.endTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    if (form.id) {
      await coachApi.updateSchedule(form.id, form)
    } else {
      await coachApi.createSchedule(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该排班吗？', '提示', { type: 'warning' })
    .then(async () => {
      await coachApi.deleteSchedule(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

watch(() => form.coachId, fetchCourses)

onMounted(async () => {
  await fetchCoaches()
  if (coaches.value.length > 0) {
    query.coachId = coaches.value[0].id
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
