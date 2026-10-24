<script setup>
//导入图标
import { Stamp, Refresh } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
//导入管理员端接口
import { getPendingVolunteersService, certifyVolunteerService } from '@/api/admin'
//分页条组件
import ListPagination from '@/components/ListPagination.vue'

const list = ref([])
const loading = ref(false)
const operating = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const loadList = async () => {
  loading.value = true
  try {
    const data = (await getPendingVolunteersService({ pageNum: pageNum.value, pageSize: pageSize.value })) || {}
    list.value = data.records || []
    total.value = data.total || 0
    //当前页被删空时自动回退一页
    if (!list.value.length && pageNum.value > 1) {
      pageNum.value -= 1
      return loadList()
    }
  } finally {
    loading.value = false
  }
}

const onPageChange = (page, size) => {
  pageNum.value = page
  pageSize.value = size
  loadList()
}

/** 审核通过/驳回（请求体字段以后端为准，这里传 { approved, reason }） */
const handleCertify = async (row, approved) => {
  let reason = ''
  if (approved) {
    try {
      await ElMessageBox.confirm(`确认通过志愿者「${row.real_name || row.username}」的实名认证吗？`, '审核确认', {
        confirmButtonText: '确认通过',
        type: 'info',
      })
    } catch {
      return
    }
  } else {
    try {
      const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回认证', {
        confirmButtonText: '确认驳回',
        cancelButtonText: '取消',
        inputValidator: (v) => (v && v.trim() ? true : '驳回原因不能为空'),
      })
      reason = value
    } catch {
      return
    }
  }

  operating.value = true
  try {
    await certifyVolunteerService(row.id ?? row.userId, { approved, reason })
    ElMessage.success(approved ? '已通过该志愿者认证' : '已驳回该志愿者认证')
    loadList()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    operating.value = false
  }
}

onMounted(loadList)
</script>

<template>
  <div class="page-card">
    <h3 class="page-title">
      <el-icon><Stamp /></el-icon>志愿者实名认证审核
      <el-badge v-if="total" :value="total" type="danger" class="pending-badge" />
      <el-button size="small" :icon="Refresh" circle @click="loadList" />
    </h3>

    <el-table v-loading="loading" :data="list">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="姓名" min-width="100">
        <template #default="{ row }">{{ row.real_name || '—' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="130">
        <template #default="{ row }">{{ row.phone || '—' }}</template>
      </el-table-column>
      <el-table-column label="身份证号" min-width="180">
        <template #default="{ row }">{{ row.id_card_no || '—' }}</template>
      </el-table-column>
      <el-table-column label="技能标签" min-width="160">
        <template #default="{ row }">
          <el-tag v-for="s in row.skillTags || []" :key="s" size="small" class="skill-tag">{{ s }}</el-tag>
          <span v-if="!(row.skillTags || []).length">—</span>
        </template>
      </el-table-column>
      <el-table-column label="服务/值班区域" min-width="160">
        <template #default="{ row }">
          {{ [row.service_region, row.duty_area].filter(Boolean).join(' / ') || '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="apply_time" label="申请时间" min-width="160">
        <template #default="{ row }">{{ row.apply_time || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" :loading="operating" @click="handleCertify(row, true)">通过</el-button>
          <el-button size="small" type="danger" :loading="operating" @click="handleCertify(row, false)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !list.length" description="暂无待审核的志愿者" />

    <ListPagination :total="total" :page="pageNum" :page-size="pageSize" @change="onPageChange" />
  </div>
</template>

<style lang="scss" scoped>
.pending-badge {
  margin-left: 8px;
}

.skill-tag {
  margin-right: 4px;
}
</style>
