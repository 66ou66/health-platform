<script setup>
//导入图标
import { Position, Refresh } from '@element-plus/icons-vue'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
//导入管理员端接口
import { getPendingRequestsService, getTasksService, assignTaskService, reassignTaskService, getUsersService } from '@/api/admin'
import { TASK_STATUS, ASSIGN_TYPE, ROLE } from '@/utils/constants'
//分页条组件
import ListPagination from '@/components/ListPagination.vue'

const activeTab = ref('pending')
const pendingList = ref([])
const taskList = ref([])
const volunteers = ref([])
const loading = ref(false)
const operating = ref(false)

//两个标签页各自的分页状态
const pendingPager = reactive({ page: 1, size: 10, total: 0 })
const taskPager = reactive({ page: 1, size: 10, total: 0 })

//派单弹窗
const assignVisible = ref(false)
const assignRow = ref(null)
const assignVolunteerId = ref(null)

//改派弹窗
const reassignVisible = ref(false)
const reassignRow = ref(null)
const reassignVolunteerId = ref(null)
const reassignRemark = ref('')

const taskStatusOf = (row) => TASK_STATUS[row.status] || { text: `状态${row.status}`, type: 'info' }
const assignTypeOf = (row) => ASSIGN_TYPE[row.assignType] || { text: `方式${row.assignType}`, type: 'info' }

const loadAll = async () => {
  loading.value = true
  try {
    const [pending, tasks] = await Promise.all([
      getPendingRequestsService({ pageNum: pendingPager.page, pageSize: pendingPager.size }),
      getTasksService({ pageNum: taskPager.page, pageSize: taskPager.size }),
    ])
    pendingList.value = pending?.records || []
    pendingPager.total = pending?.total || 0
    taskList.value = tasks?.records || []
    taskPager.total = tasks?.total || 0
    //当前页被删空时自动回退一页
    let back = false
    if (!pendingList.value.length && pendingPager.page > 1) { pendingPager.page -= 1; back = true }
    if (!taskList.value.length && taskPager.page > 1) { taskPager.page -= 1; back = true }
    if (back) return loadAll()
  } finally {
    loading.value = false
  }
}

const onPendingPage = (page, size) => {
  pendingPager.page = page
  pendingPager.size = size
  loadAll()
}

const onTaskPage = (page, size) => {
  taskPager.page = page
  taskPager.size = size
  loadAll()
}

/** 加载志愿者选项（后端按角色过滤，前端再兜底筛选一次） */
const loadVolunteers = async () => {
  try {
    const data = (await getUsersService({ role: ROLE.VOLUNTEER, pageNum: 1, pageSize: 100 })) || {}
    volunteers.value = (data.records || []).filter((u) => u.role === ROLE.VOLUNTEER)
  } catch {
    volunteers.value = []
  }
}

/** 打开派单弹窗 */
const openAssign = (row) => {
  assignRow.value = row
  assignVolunteerId.value = null
  assignVisible.value = true
  loadVolunteers()
}

/** 确认派单（系统派单，assign_type=2） */
const confirmAssign = async () => {
  if (!assignVolunteerId.value) {
    ElMessage.warning('请选择志愿者')
    return
  }
  operating.value = true
  try {
    await assignTaskService({
      requestId: assignRow.value.id,
      volunteerId: assignVolunteerId.value,
    })
    ElMessage.success('派单成功，志愿者可在"我的任务"中查看')
    assignVisible.value = false
    loadAll()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    operating.value = false
  }
}

/** 打开改派弹窗 */
const openReassign = (row) => {
  reassignRow.value = row
  reassignVolunteerId.value = null
  reassignRemark.value = ''
  reassignVisible.value = true
  loadVolunteers()
}

/** 确认改派 */
const confirmReassign = async () => {
  if (!reassignVolunteerId.value) {
    ElMessage.warning('请选择新的志愿者')
    return
  }
  if (!reassignRemark.value.trim()) {
    ElMessage.warning('请填写改派原因')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定将任务改派给其他志愿者吗？原志愿者将无法继续处理该任务。`,
      '改派确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  operating.value = true
  try {
    await reassignTaskService(reassignRow.value.id, {
      volunteerId: reassignVolunteerId.value,
      remark: reassignRemark.value,
    })
    ElMessage.success('改派成功')
    reassignVisible.value = false
    loadAll()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    operating.value = false
  }
}

onMounted(loadAll)
</script>

<template>
  <div class="page-card">
    <h3 class="page-title">
      <el-icon><Position /></el-icon>派单管理
      <el-button size="small" :icon="Refresh" circle @click="loadAll" />
    </h3>

    <el-tabs v-model="activeTab">
      <!-- 待派单需求 -->
      <el-tab-pane label="待派单需求" name="pending">
        <el-table v-loading="loading" :data="pendingList">
          <el-table-column label="服务类型" min-width="110">
            <template #default="{ row }">{{ row.category_name || row.categoryName || '-' }}</template>
          </el-table-column>
          <el-table-column label="标题" min-width="150">
            <template #default="{ row }">
              <el-tag v-if="row.emergency_flag === 1" type="danger" size="small">紧急</el-tag>
              {{ row.title || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="患者" min-width="90">
            <template #default="{ row }">{{ row.patient_name || '-' }}</template>
          </el-table-column>
          <el-table-column prop="appointment_time" label="约定时间" min-width="160" />
          <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openAssign(row)">派单</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && !pendingList.length" description="当前没有待派单的需求" />

        <ListPagination :total="pendingPager.total" :page="pendingPager.page" :page-size="pendingPager.size" @change="onPendingPage" />
      </el-tab-pane>

      <!-- 已派任务 -->
      <el-tab-pane label="已派任务" name="tasks">
        <el-table v-loading="loading" :data="taskList">
          <el-table-column prop="id" label="任务编号" width="90" />
          <el-table-column label="服务类型" min-width="110">
            <template #default="{ row }">{{ row.category_name || row.categoryName || '-' }}</template>
          </el-table-column>
          <el-table-column label="标题" min-width="140">
            <template #default="{ row }">{{ row.title || '-' }}</template>
          </el-table-column>
          <el-table-column label="患者" min-width="90">
            <template #default="{ row }">{{ row.patient_name || '-' }}</template>
          </el-table-column>
          <el-table-column label="志愿者" min-width="90">
            <template #default="{ row }">{{ row.volunteer_name || '-' }}</template>
          </el-table-column>
          <el-table-column label="方式" width="110">
            <template #default="{ row }">
              <el-tag :type="assignTypeOf(row).type" size="small">{{ assignTypeOf(row).text }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="taskStatusOf(row).type" size="small">{{ taskStatusOf(row).text }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="改派备注" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">{{ row.admin_remark || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button v-if="[0, 1].includes(row.status)" size="small" type="warning" @click="openReassign(row)">
                改派
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && !taskList.length" description="暂无任务记录" />

        <ListPagination :total="taskPager.total" :page="taskPager.page" :page-size="taskPager.size" @change="onTaskPage" />
      </el-tab-pane>
    </el-tabs>
  </div>

  <!-- 派单弹窗 -->
  <el-dialog v-model="assignVisible" title="系统派单" width="440px">
    <el-alert
      :title="`需求：${assignRow?.title || ''} ｜ 患者：${assignRow?.patient_name || ''}`"
      type="info"
      :closable="false"
      show-icon
      class="assign-alert"
    />
    <el-form label-width="80px">
      <el-form-item label="选择志愿者">
        <el-select v-model="assignVolunteerId" placeholder="请选择已通过认证的志愿者" style="width: 100%">
          <el-option
            v-for="v in volunteers"
            :key="v.id"
            :label="v.realName || v.username"
            :value="v.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="assignVisible = false">取消</el-button>
      <el-button type="primary" :loading="operating" @click="confirmAssign">确认派单</el-button>
    </template>
  </el-dialog>

  <!-- 改派弹窗 -->
  <el-dialog v-model="reassignVisible" title="系统改派" width="440px">
    <el-alert
      :title="`任务：${reassignRow?.title || ''} ｜ 当前志愿者：${reassignRow?.volunteer_name || ''}`"
      type="warning"
      :closable="false"
      show-icon
      class="assign-alert"
    />
    <el-form label-width="80px">
      <el-form-item label="新志愿者">
        <el-select v-model="reassignVolunteerId" placeholder="请选择已通过认证的志愿者" style="width: 100%">
          <el-option
            v-for="v in volunteers"
            :key="v.id"
            :label="v.realName || v.username"
            :value="v.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="改派原因">
        <el-input v-model="reassignRemark" type="textarea" :rows="2" maxlength="200" placeholder="请填写改派原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="reassignVisible = false">取消</el-button>
      <el-button type="warning" :loading="operating" @click="confirmReassign">确认改派</el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.assign-alert {
  margin-bottom: 16px;
}
</style>
