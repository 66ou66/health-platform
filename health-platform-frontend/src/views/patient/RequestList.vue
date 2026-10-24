<script setup>
//导入图标
import { Tickets } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
//导入患者端接口
import { getMyRequestsService, getRequestDetailService, cancelRequestService, submitFeedbackService } from '@/api/patient'
import { REQUEST_STATUS } from '@/utils/constants'
//分页条组件
import ListPagination from '@/components/ListPagination.vue'

const requests = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

//详情弹窗
const detailVisible = ref(false)
const detail = ref(null)
const detailLoading = ref(false)

//评价弹窗
const evaluateVisible = ref(false)
const evaluatingRow = ref(null)
const evaluating = ref(false)
const rating = ref(5)
const evaluateContent = ref('')
const evaluatePublic = ref(true)

/** 可取消的状态：待接单 / 已接单 */
const canCancel = (status) => [0, 1].includes(status)
/** 可评价的状态：已完成 */
const canEvaluate = (status) => status === 3

const loadRequests = async () => {
  loading.value = true
  try {
    const data = (await getMyRequestsService({ pageNum: pageNum.value, pageSize: pageSize.value })) || {}
    requests.value = data.records || []
    total.value = data.total || 0
    //当前页被删空时自动回退一页
    if (!requests.value.length && pageNum.value > 1) {
      pageNum.value -= 1
      return loadRequests()
    }
  } finally {
    loading.value = false
  }
}

const onPageChange = (page, size) => {
  pageNum.value = page
  pageSize.value = size
  loadRequests()
}

/** 查看需求详情 */
const showDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await getRequestDetailService(row.id ?? row.requestId)
  } finally {
    detailLoading.value = false
  }
}

/** 取消需求 */
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该需求吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await cancelRequestService(row.id ?? row.requestId)
    ElMessage.success('需求已取消')
    loadRequests()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  }
}

/** 打开评价弹窗 */
const openEvaluate = (row) => {
  evaluatingRow.value = row
  rating.value = 5
  evaluateContent.value = ''
  evaluatePublic.value = true
  evaluateVisible.value = true
}

/** 提交评价（评价接单的志愿者） */
const submitEvaluate = async () => {
  const taskId = evaluatingRow.value?.task_id ?? evaluatingRow.value?.taskId
  if (!taskId) {
    ElMessage.warning('该需求还没有任务记录，无法评价')
    evaluateVisible.value = false
    return
  }
  evaluating.value = true
  try {
    await submitFeedbackService({
      taskId,
      rating: rating.value,
      content: evaluateContent.value,
      isPublic: evaluatePublic.value ? 1 : 0,
    })
    ElMessage.success('评价提交成功，感谢您的反馈！')
    evaluateVisible.value = false
    loadRequests()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    evaluating.value = false
  }
}

const statusOf = (row) => REQUEST_STATUS[row.status] || { text: `状态${row.status}`, type: 'info' }

onMounted(loadRequests)
</script>

<template>
  <div class="page-card">
    <h3 class="page-title"><el-icon><Tickets /></el-icon>我的需求</h3>

    <el-table v-loading="loading" :data="requests">
      <el-table-column prop="id" label="编号" width="70" />
      <el-table-column label="需求" min-width="180">
        <template #default="{ row }">
          <el-tag v-if="row.emergency_flag === 1" type="danger" size="small">紧急</el-tag>
          {{ row.title }}
        </template>
      </el-table-column>
      <el-table-column label="服务类型" min-width="100">
        <template #default="{ row }">{{ row.category_name || row.categoryId || '-' }}</template>
      </el-table-column>
      <el-table-column prop="appointment_time" label="约定时间" min-width="160" />
      <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip />
      <el-table-column label="志愿者" min-width="90">
        <template #default="{ row }">{{ row.volunteer_name || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusOf(row).type" size="small">{{ statusOf(row).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="created_at" label="发布时间" min-width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="showDetail(row)">详情</el-button>
          <el-button v-if="canEvaluate(row.status)" size="small" link type="success" @click="openEvaluate(row)">
            评价
          </el-button>
          <el-button v-if="canCancel(row.status)" size="small" link type="danger" @click="handleCancel(row)">
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !requests.length" description="暂无需求记录，去发布一个吧" />

    <ListPagination :total="total" :page="pageNum" :page-size="pageSize" @change="onPageChange" />
  </div>

  <!-- 需求详情弹窗 -->
  <el-dialog v-model="detailVisible" title="需求详情" width="480px" v-loading="detailLoading">
    <el-descriptions v-if="detail" :column="1" border>
      <el-descriptions-item label="需求标题">{{ detail.title }}</el-descriptions-item>
      <el-descriptions-item label="服务类型">{{ detail.category_name || detail.categoryId }}</el-descriptions-item>
      <el-descriptions-item label="约定时间">{{ detail.appointment_time }}</el-descriptions-item>
      <el-descriptions-item label="服务地址">{{ detail.address }}</el-descriptions-item>
      <el-descriptions-item label="需求描述">{{ detail.description || '—' }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ statusOf(detail).text }}</el-descriptions-item>
      <template v-if="detail.volunteer_name">
        <el-descriptions-item label="接单志愿者">{{ detail.volunteer_name }}</el-descriptions-item>
      </template>
    </el-descriptions>
    <el-empty v-else-if="!detailLoading" description="暂无详情数据" />
  </el-dialog>

  <!-- 评价弹窗 -->
  <el-dialog v-model="evaluateVisible" title="服务评价" width="420px">
    <div class="evaluate-item">
      <span class="evaluate-label">服务评分</span>
      <el-rate v-model="rating" />
    </div>
    <div class="evaluate-item">
      <span class="evaluate-label">评价内容</span>
      <el-input
        v-model="evaluateContent"
        type="textarea"
        :rows="4"
        maxlength="200"
        show-word-limit
        placeholder="说说这次服务的感受吧"
      />
    </div>
    <div class="evaluate-item">
      <span class="evaluate-label">公开评价</span>
      <el-switch v-model="evaluatePublic" active-text="公开" inactive-text="不公开" />
    </div>
    <template #footer>
      <el-button @click="evaluateVisible = false">取消</el-button>
      <el-button type="primary" :loading="evaluating" @click="submitEvaluate">提交评价</el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.evaluate-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;

  .evaluate-label {
    width: 64px;
    line-height: 32px;
    flex-shrink: 0;
  }
}
</style>
