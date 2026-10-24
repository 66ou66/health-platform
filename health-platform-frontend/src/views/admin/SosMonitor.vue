<script setup>
//导入图标
import { BellFilled, Refresh } from '@element-plus/icons-vue'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
//导入管理员端接口
import { getPendingSosService, acceptSosService, completeSosService, getUsersService } from '@/api/admin'
import { SOS_STATUS, ROLE } from '@/utils/constants'
//分页条组件
import ListPagination from '@/components/ListPagination.vue'

const list = ref([])
const volunteers = ref([])
const loading = ref(false)
const operating = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

/** 5 秒轮询（对应报告中的实时通知方案） */
let pollTimer = null
const POLL_INTERVAL = 5000

// 指派弹窗
const assignVisible = ref(false)
const assignSos = ref(null)
const assignVolunteerId = ref(null)

//后端仅返回待处理（status 0/1）记录，total 即待处理总数
const sosCount = computed(() => total.value)

const statusOf = (row) => SOS_STATUS[row.status] || { text: `状态${row.status}`, type: 'info' }

const loadSos = async () => {
  // 仅首次加载显示 loading，5 秒轮询刷新时不闪烁
  loading.value = list.value.length === 0
  try {
    const data = (await getPendingSosService({ pageNum: pageNum.value, pageSize: pageSize.value })) || {}
    list.value = data.records || []
    total.value = data.total || 0
    //当前页被处理完后自动回退一页（轮询刷新时同样生效）
    if (!list.value.length && pageNum.value > 1) {
      pageNum.value -= 1
      return loadSos()
    }
  } finally {
    loading.value = false
  }
}

const onPageChange = (page, size) => {
  pageNum.value = page
  pageSize.value = size
  loadSos()
}

/** 加载可指派的志愿者（后端按角色过滤，前端再兜底筛选一次） */
const loadVolunteers = async () => {
  try {
    const data = (await getUsersService({ role: ROLE.VOLUNTEER, pageNum: 1, pageSize: 100 })) || {}
    volunteers.value = (data.records || []).filter((u) => u.role === ROLE.VOLUNTEER)
  } catch {
    volunteers.value = []
  }
}

/** 打开指派弹窗 */
const openAssign = (row) => {
  assignSos.value = row
  assignVolunteerId.value = null
  assignVisible.value = true
  loadVolunteers()
}

/** 确认指派（请求体字段以后端为准，这里传 { volunteerId }） */
const confirmAssign = async () => {
  if (!assignVolunteerId.value) {
    ElMessage.warning('请选择接警的志愿者')
    return
  }
  operating.value = true
  try {
    await acceptSosService(assignSos.value.id ?? assignSos.value.sosId, { volunteerId: assignVolunteerId.value })
    ElMessage.success('已指派志愿者前往处理')
    assignVisible.value = false
    loadSos()
  } finally {
    operating.value = false
  }
}

/** 完成 SOS */
const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认「${row.patient_name || row.address || row.id}」的求助已处理完毕吗？`,
      '完成确认',
      { type: 'info', confirmButtonText: '确认完成' },
    )
  } catch {
    return
  }
  operating.value = true
  try {
    await completeSosService(row.id ?? row.sosId)
    ElMessage.success('该求助已标记完成')
    loadSos()
  } finally {
    operating.value = false
  }
}

onMounted(() => {
  loadSos()
  // 5 秒轮询保持实时监控
  pollTimer = setInterval(loadSos, POLL_INTERVAL)
})

onUnmounted(() => {
  clearInterval(pollTimer)
})
</script>

<template>
  <div class="page-card sos-page">
    <h3 class="page-title sos-title">
      <el-icon><BellFilled /></el-icon>SOS 紧急求助监控
      <el-tag v-if="sosCount" type="danger" effect="dark" class="alarm-tag">
        {{ sosCount }} 条待处理
      </el-tag>
      <span v-else class="quiet-tip">当前无待处理求助</span>
      <el-button size="small" :icon="Refresh" circle @click="loadSos" />
      <span class="poll-tip">每 5 秒自动刷新</span>
    </h3>

    <el-table v-loading="loading" :data="list">
      <el-table-column prop="id" label="编号" width="70" />
      <el-table-column label="求助人" min-width="100">
        <template #default="{ row }">{{ row.patient_name || '—' }}</template>
      </el-table-column>
      <el-table-column prop="address" label="位置（逆地理编码）" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ row.address || '—' }}</template>
      </el-table-column>
      <el-table-column label="联系电话" min-width="130">
        <template #default="{ row }">{{ row.contact_phone || '—' }}</template>
      </el-table-column>
      <el-table-column label="情况备注" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.remark || '—' }}</template>
      </el-table-column>
      <el-table-column prop="trigger_time" label="触发时间" min-width="160">
        <template #default="{ row }">{{ row.trigger_time || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusOf(row).type" size="small" effect="dark">{{ statusOf(row).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理志愿者" min-width="100">
        <template #default="{ row }">{{ row.volunteer_name || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="[0].includes(row.status)" size="small" type="danger" @click="openAssign(row)">
            指派
          </el-button>
          <el-button v-if="[0, 1].includes(row.status)" size="small" type="success" :loading="operating" @click="handleComplete(row)">
            完成
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !list.length" description="暂无 SOS 求助记录" />

    <ListPagination :total="total" :page="pageNum" :page-size="pageSize" @change="onPageChange" />
  </div>

  <!-- 指派志愿者弹窗 -->
  <el-dialog v-model="assignVisible" title="指派志愿者前往处理" width="440px">
    <el-alert
      :title="`求助人：${assignSos?.patient_name || '未知'} ｜ 位置：${assignSos?.address || '未知'} ｜ 电话：${assignSos?.contact_phone || '未知'}`"
      type="error"
      :closable="false"
      show-icon
      class="assign-alert"
    />
    <el-form label-width="80px">
      <el-form-item label="选择志愿者">
        <el-select v-model="assignVolunteerId" placeholder="请选择院内志愿者" style="width: 100%">
          <el-option
            v-for="v in volunteers"
            :key="v.id ?? v.userId"
            :label="v.realName || v.username"
            :value="v.id ?? v.userId"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="assignVisible = false">取消</el-button>
      <el-button type="danger" :loading="operating" @click="confirmAssign">确认指派</el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.sos-page {
  border: 1px solid #f6c9c8;
}

.sos-title {
  color: var(--sos-color);
}

.alarm-tag {
  animation: sos-blink 1.2s infinite;
}

.quiet-tip {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: normal;
}

.poll-tip {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: normal;
}

.assign-alert {
  margin-bottom: 16px;
}

@keyframes sos-blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.4;
  }
}
</style>
