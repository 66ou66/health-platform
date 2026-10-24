<script setup>
//导入图标
import { User, Avatar, Medal, Stamp, Loading, BellFilled, CircleCheckFilled, TrendCharts, DataAnalysis } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
//导入管理员端接口
import { getDashboardService } from '@/api/admin'

const loading = ref(false)
const dash = ref(null)

/**
 * /admin/dashboard 返回结构以后端为准，
 * 下方卡片取值均为推测字段，联调时按实际字段名调整即可；
 * 底部会以键值对形式兜底展示后端返回的全部数据。
 */
const cards = [
  { key: 'userCount', label: '用户总数', icon: User, color: '#309bcc' },
  { key: 'patientCount', label: '患者人数', icon: Avatar, color: '#409eff' },
  { key: 'volunteerCount', label: '志愿者人数', icon: Medal, color: '#e6a23c' },
  { key: 'pendingVolunteerCount', label: '待审核志愿者', icon: Stamp, color: '#f56c6c' },
  { key: 'doingTaskCount', label: '进行中任务', icon: Loading, color: '#67c23a' },
  { key: 'pendingSosCount', label: '待处理 SOS', icon: BellFilled, color: '#e64340' },
  { key: 'doneTaskCount', label: '已完成任务', icon: CircleCheckFilled, color: '#67c23a' },
  { key: 'healthRecordCount', label: '健康记录总数', icon: TrendCharts, color: '#909399' },
]

const load = async () => {
  loading.value = true
  try {
    dash.value = (await getDashboardService()) ?? {}
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.key" :span="6">
        <div class="page-card dash-card">
          <el-icon :size="40" :color="card.color" class="dash-icon"><component :is="card.icon" /></el-icon>
          <div>
            <div class="dash-num">{{ dash?.[card.key] ?? '—' }}</div>
            <div class="dash-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 后端返回数据兜底展示：字段名不一致时可在此核对 -->
    <div class="page-card" v-if="dash && Object.keys(dash).length">
      <h3 class="page-title"><el-icon><DataAnalysis /></el-icon>看板原始数据（用于联调核对字段）</h3>
      <el-descriptions :column="4" border>
        <el-descriptions-item v-for="(value, key) in dash" :key="key" :label="key">
          {{ value }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dash-card {
  display: flex;
  align-items: center;
  gap: 16px;

  .dash-icon {
    flex-shrink: 0;
  }

  .dash-num {
    font-size: 28px;
    font-weight: 700;
    line-height: 1.2;
  }

  .dash-label {
    font-size: 13px;
    color: var(--text-secondary);
  }
}
</style>
