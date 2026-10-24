<script setup>
//导入图标
import { User, Refresh } from '@element-plus/icons-vue'
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
//导入管理员端接口
import { getUsersService, updateUserStatusService, getPatientFullProfileService } from '@/api/admin'
import { ROLE, ROLE_TEXT, ROLE_TAG_TYPE, USER_STATUS } from '@/utils/constants'
//分页条组件
import ListPagination from '@/components/ListPagination.vue'

const users = ref([])
const loading = ref(false)
const filterRole = ref(null)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

//患者完整档案弹窗
const profileVisible = ref(false)
const profile = ref(null)
const profileLoading = ref(false)

const statusOf = (row) => USER_STATUS[row.status] || { text: `状态${row.status}`, type: 'info' }

const loadUsers = async () => {
  loading.value = true
  try {
    const data = (await getUsersService({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      role: filterRole.value ?? undefined,
    })) || {}
    users.value = data.records || []
    total.value = data.total || 0
    //当前页被删空时自动回退一页
    if (!users.value.length && pageNum.value > 1) {
      pageNum.value -= 1
      return loadUsers()
    }
  } finally {
    loading.value = false
  }
}

const onPageChange = (page, size) => {
  pageNum.value = page
  pageSize.value = size
  loadUsers()
}

//切换角色筛选时回到第一页
watch(filterRole, () => {
  pageNum.value = 1
  loadUsers()
})

/** 冻结/解冻（1正常 0冻结，请求体字段以后端为准） */
const toggleStatus = async (row) => {
  const freezing = row.status !== 0
  try {
    await ElMessageBox.confirm(
      freezing ? `确定要冻结用户「${row.username}」吗？冻结后该用户将无法登录。` : `确定要解冻用户「${row.username}」吗？`,
      freezing ? '冻结确认' : '解冻确认',
      { type: 'warning' },
    )
  } catch {
    return
  }
  try {
    await updateUserStatusService(row.id ?? row.userId, { status: freezing ? 0 : 1 })
    ElMessage.success(freezing ? '已冻结该用户' : '已解冻该用户')
    loadUsers()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  }
}

/** 查看患者完整档案（仅患者角色可用，含身份证号/慢性病/服药清单等敏感字段） */
const showProfile = async (row) => {
  profileVisible.value = true
  profileLoading.value = true
  profile.value = null
  try {
    profile.value = await getPatientFullProfileService(row.id ?? row.userId)
  } finally {
    profileLoading.value = false
  }
}

onMounted(loadUsers)
</script>

<template>
  <div class="page-card">
    <h3 class="page-title">
      <el-icon><User /></el-icon>用户管理
      <el-select v-model="filterRole" placeholder="按角色筛选" clearable size="small" style="width: 140px">
        <el-option v-for="(text, value) in ROLE_TEXT" :key="value" :label="text" :value="Number(value)" />
      </el-select>
      <el-button size="small" :icon="Refresh" circle @click="loadUsers" />
    </h3>

    <el-table v-loading="loading" :data="users">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="100">
        <template #default="{ row }">{{ row.realName || '—' }}</template>
      </el-table-column>
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="ROLE_TAG_TYPE[row.role] || 'info'" size="small">
            {{ ROLE_TEXT[row.role] || row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="130">
        <template #default="{ row }">{{ row.phone || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusOf(row).type" size="small">{{ statusOf(row).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" min-width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.role === ROLE.PATIENT" size="small" link type="primary" @click="showProfile(row)">
            查看档案
          </el-button>
          <el-button size="small" link :type="row.status !== 0 ? 'danger' : 'success'" @click="toggleStatus(row)">
            {{ row.status !== 0 ? '冻结' : '解冻' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <ListPagination :total="total" :page="pageNum" :page-size="pageSize" @change="onPageChange" />
  </div>

  <!-- 患者完整档案弹窗（管理员权限：含全部敏感字段） -->
  <el-dialog v-model="profileVisible" title="患者完整档案" width="560px" v-loading="profileLoading">
    <template v-if="profile">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="姓名">{{ profile.realName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ profile.gender === 1 ? '男' : profile.gender === 0 ? '女' : '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ profile.birthday || '—' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ profile.idCardNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="慢性病史">{{ profile.chronicDiseases || '无' }}</el-descriptions-item>
        <el-descriptions-item label="服药清单">{{ profile.medicationList || '无' }}</el-descriptions-item>
        <el-descriptions-item label="过敏史">{{ profile.allergyHistory || '无' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人">
          {{ profile.emergencyContactName || '—' }} {{ profile.emergencyContactPhone || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="家庭住址">{{ profile.homeAddress || '—' }}</el-descriptions-item>
        <el-descriptions-item label="院内位置">
          {{ [profile.building, profile.floor, profile.room].filter(Boolean).join(' ') || '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </template>
    <el-empty v-else-if="!profileLoading" description="暂无档案数据" />
  </el-dialog>
</template>
