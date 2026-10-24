<script setup>
//导入图标
import { CollectionTag, Plus, Refresh } from '@element-plus/icons-vue'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
//导入管理员端接口
import { getCategoriesService, addCategoryService, updateCategoryService, deleteCategoryService } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const saving = ref(false)

//新增/编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const editingId = ref(null)
const formRef = ref()
const form = reactive({ categoryName: '', icon: '', sort: 0 })

const rules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 20, message: '分类名称不超过 20 字', trigger: 'blur' },
  ],
}

const loadList = async () => {
  loading.value = true
  try {
    list.value = (await getCategoriesService()) || []
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  dialogTitle.value = '新增分类'
  editingId.value = null
  form.categoryName = ''
  form.icon = ''
  form.sort = 0
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogTitle.value = '编辑分类'
  editingId.value = row.id ?? row.categoryId
  form.categoryName = row.categoryName
  form.icon = row.icon || ''
  form.sort = row.sort ?? 0
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editingId.value == null) {
      await addCategoryService({ ...form })
      ElMessage.success('分类新增成功')
    } else {
      await updateCategoryService({ id: editingId.value, ...form })
      ElMessage.success('分类修改成功')
    }
    dialogVisible.value = false
    loadList()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${row.categoryName}」吗？已被需求引用的分类删除后可能无法展示。`,
      '删除确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await deleteCategoryService(row.id ?? row.categoryId)
    ElMessage.success('分类已删除')
    loadList()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  }
}

onMounted(loadList)
</script>

<template>
  <div class="page-card">
    <h3 class="page-title">
      <el-icon><CollectionTag /></el-icon>服务分类管理
      <el-button type="primary" size="small" :icon="Plus" @click="openAdd">新增分类</el-button>
      <el-button size="small" :icon="Refresh" circle @click="loadList" />
    </h3>

    <el-table v-loading="loading" :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="icon" label="图标" width="90">
        <template #default="{ row }">{{ row.icon || '—' }}</template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类名称" min-width="140" />
      <el-table-column prop="sort" label="排序" width="90">
        <template #default="{ row }">{{ row.sort ?? '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !list.length" description="暂无分类，点击右上角新增" />
  </div>

  <!-- 新增/编辑弹窗 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="420px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="form.categoryName" placeholder="例如：陪诊就医、买药送药" maxlength="20" />
      </el-form-item>
      <el-form-item label="图标">
        <el-input v-model="form.icon" placeholder="emoji 图标（选填），例如：🏥" maxlength="20" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>
