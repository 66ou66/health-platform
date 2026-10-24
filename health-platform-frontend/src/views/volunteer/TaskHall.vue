<script setup>
//导入图标
import { Grid, Refresh } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
//导入志愿者端接口
import { getPendingTasksService, claimTaskService } from '@/api/volunteer'
//分页条组件
import ListPagination from '@/components/ListPagination.vue'

const tasks = ref([])
const loading = ref(false)
const claiming = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

//详情弹窗
const detailVisible = ref(false)
const current = ref(null)

const loadTasks = async () => {
  loading.value = true
  try {
    const data = (await getPendingTasksService({ pageNum: pageNum.value, pageSize: pageSize.value })) || {}
    tasks.value = data.records || []
    total.value = data.total || 0
    //当前页被抢完后自动回退一页
    if (!tasks.value.length && pageNum.value > 1) {
      pageNum.value -= 1
      return loadTasks()
    }
  } finally {
    loading.value = false
  }
}

const onPageChange = (page, size) => {
  pageNum.value = page
  pageSize.value = size
  loadTasks()
}

const showDetail = (row) => {
  current.value = row
  detailVisible.value = true
}

/** 根据出生日期计算年龄 */
const ageOf = (row) => {
  const birthday = row.patient_birthday
  if (!birthday) return '-'
  const diff = Date.now() - new Date(birthday).getTime()
  return Math.floor(diff / (365.25 * 24 * 3600 * 1000))
}

/** 抢单 */
const handleClaim = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认接下「${row.title || row.category_name}」这项服务吗？接单后请按时签到并完成服务。`,
      '抢单确认',
      { confirmButtonText: '确认抢单', cancelButtonText: '再想想', type: 'info' },
    )
  } catch {
    return
  }
  claiming.value = true
  try {
    await claimTaskService(row.id ?? row.requestId)
    ElMessage.success('抢单成功！请在服务开始前完成签到')
    detailVisible.value = false
    loadTasks()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    claiming.value = false
  }
}

/**
 * 患者脱敏信息展示（对应后端权限边界）：
 * 志愿者仅可见 姓名/年龄/性别/过敏史/紧急联系人/地址，
 * 不含身份证号、慢性病、服药清单等敏感字段（后端脱敏后返回）。
 */
const patientItems = (row) => [
  { label: '姓名', value: row.patient_name || '-' },
  { label: '年龄', value: ageOf(row) },
  { label: '性别', value: row.patient_gender === 1 ? '男' : row.patient_gender === 0 ? '女' : '-' },
  { label: '过敏史', value: row.allergy_history || '无' },
  { label: '紧急联系人', value: `${row.emergency_contact_name || '—'} ${row.emergency_contact_phone || ''}` },
  { label: '地址', value: row.address || '-' },
]

onMounted(loadTasks)
</script>

<template>
  <div class="page-card">
    <h3 class="page-title">
      <el-icon><Grid /></el-icon>任务大厅
      <el-button size="small" :icon="Refresh" circle @click="loadTasks" />
    </h3>

    <el-table v-loading="loading" :data="tasks">
      <el-table-column label="服务类型" min-width="100">
        <template #default="{ row }">{{ row.category_name || row.categoryId || '-' }}</template>
      </el-table-column>
      <el-table-column label="需求" min-width="170">
        <template #default="{ row }">
          <el-tag v-if="row.emergency_flag === 1" type="danger" size="small">紧急</el-tag>
          {{ row.title || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="appointment_time" label="约定时间" min-width="160" />
      <el-table-column prop="address" label="服务地址" min-width="180" show-overflow-tooltip />
      <el-table-column label="发布时间" min-width="160">
        <template #default="{ row }">{{ row.created_at || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="showDetail(row)">详情</el-button>
          <el-button size="small" type="primary" :loading="claiming" @click="handleClaim(row)">抢单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !tasks.length" description="当前没有待接单的需求，休息一下吧" />

    <ListPagination :total="total" :page="pageNum" :page-size="pageSize" @change="onPageChange" />
  </div>

  <!-- 需求详情弹窗（含患者脱敏信息） -->
  <el-dialog v-model="detailVisible" title="需求详情" width="520px">
    <template v-if="current">
      <el-descriptions title="需求信息" :column="1" border>
        <el-descriptions-item label="服务类型">{{ current.category_name || current.categoryId }}</el-descriptions-item>
        <el-descriptions-item label="需求标题">{{ current.title }}</el-descriptions-item>
        <el-descriptions-item label="约定时间">{{ current.appointment_time }}</el-descriptions-item>
        <el-descriptions-item label="服务地址">{{ current.address }}</el-descriptions-item>
        <el-descriptions-item label="需求描述">{{ current.description || '—' }}</el-descriptions-item>
      </el-descriptions>
      <el-descriptions title="患者信息（脱敏）" :column="1" border class="patient-desc">
        <el-descriptions-item v-for="item in patientItems(current)" :key="item.label" :label="item.label">
          {{ item.value }}
        </el-descriptions-item>
      </el-descriptions>
    </template>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
      <el-button type="primary" :loading="claiming" @click="handleClaim(current)">抢单</el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.patient-desc {
  margin-top: 16px;
}
</style>
