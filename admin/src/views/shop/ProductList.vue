<template>
  <div class="page">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="handleAdd">新增商品</el-button>
    </div>

    <el-form inline class="search-form">
      <el-form-item label="分类">
        <el-select v-model="query.categoryId" placeholder="选择分类" @change="fetchList" clearable>
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="商品名称" @keyup.enter="fetchList" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchList">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="categoryName" label="分类" />
      <el-table-column prop="subtitle" label="副标题" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="warning" size="small" @click="handleManageSku(row)">SKU</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- SPU 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" />
        </el-form-item>
        <el-form-item label="主图URL">
          <el-input v-model="form.mainImage" placeholder="图片地址" />
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="form.detail" type="textarea" rows="4" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- SKU 管理弹窗 -->
    <el-dialog v-model="skuDialogVisible" title="SKU 管理" width="800px">
      <div style="margin-bottom: 12px;">
        <el-button type="primary" size="small" @click="handleAddSku">新增SKU</el-button>
      </div>
      <el-table :data="skuList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="skuCode" label="SKU编码" />
        <el-table-column prop="specs" label="规格" />
        <el-table-column prop="price" label="售价" />
        <el-table-column prop="stock" label="库存" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditSku(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteSku(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- SKU 编辑表单 -->
      <el-dialog v-model="skuFormVisible" :title="skuForm.id ? '编辑SKU' : '新增SKU'" width="500px" append-to-body>
        <el-form :model="skuForm" label-width="100px">
          <el-form-item label="SKU编码">
            <el-input v-model="skuForm.skuCode" />
          </el-form-item>
          <el-form-item label="规格">
            <el-input v-model="skuForm.specs" placeholder='JSON 格式，如 {"颜色":"红色","尺码":"L"}' />
          </el-form-item>
          <el-form-item label="售价">
            <el-input-number v-model="skuForm.price" :min="0" :precision="2" />
          </el-form-item>
          <el-form-item label="库存">
            <el-input-number v-model="skuForm.stock" :min="0" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="skuForm.status">
              <el-radio :label="1">上架</el-radio>
              <el-radio :label="0">下架</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="skuFormVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitSku">确定</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shopApi } from '../../api/shop'

const list = ref([])
const categories = ref([])
const skuList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const skuDialogVisible = ref(false)
const skuFormVisible = ref(false)
const currentSpuId = ref(null)

const query = reactive({
  categoryId: '',
  keyword: ''
})

const form = reactive({
  id: null,
  categoryId: '',
  name: '',
  subtitle: '',
  mainImage: '',
  detail: '',
  status: 1,
  sortOrder: 0
})

const skuForm = reactive({
  id: null,
  spuId: '',
  skuCode: '',
  specs: '{}',
  price: 0,
  stock: 0,
  status: 1
})

const fetchCategories = async () => {
  categories.value = await shopApi.listCategories()
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {}
    if (query.categoryId) params.categoryId = query.categoryId
    if (query.keyword) params.keyword = query.keyword
    list.value = await shopApi.listProducts(params)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null, categoryId: '', name: '', subtitle: '',
    mainImage: '', detail: '', status: 1, sortOrder: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.categoryId || !form.name) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    if (form.id) {
      await shopApi.updateProduct(form.id, form)
    } else {
      await shopApi.createProduct(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) { console.error(e) }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该商品吗？', '提示', { type: 'warning' })
    .then(async () => {
      await shopApi.deleteProduct(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

const handleManageSku = async (row) => {
  currentSpuId.value = row.id
  skuList.value = await shopApi.listSkus(row.id)
  skuDialogVisible.value = true
}

const handleAddSku = () => {
  Object.assign(skuForm, {
    id: null, spuId: currentSpuId.value, skuCode: '',
    specs: '{}', price: 0, stock: 0, status: 1
  })
  skuFormVisible.value = true
}

const handleEditSku = (row) => {
  Object.assign(skuForm, row)
  skuFormVisible.value = true
}

const handleSubmitSku = async () => {
  if (!skuForm.skuCode || !skuForm.specs) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    if (skuForm.id) {
      await shopApi.updateSku(skuForm.id, skuForm)
    } else {
      await shopApi.createSku(skuForm)
    }
    ElMessage.success('保存成功')
    skuFormVisible.value = false
    skuList.value = await shopApi.listSkus(currentSpuId.value)
  } catch (e) { console.error(e) }
}

const handleDeleteSku = (row) => {
  ElMessageBox.confirm('确认删除该SKU吗？', '提示', { type: 'warning' })
    .then(async () => {
      await shopApi.deleteSku(row.id)
      ElMessage.success('删除成功')
      skuList.value = await shopApi.listSkus(currentSpuId.value)
    })
    .catch(() => {})
}

onMounted(async () => {
  await fetchCategories()
  fetchList()
})
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
