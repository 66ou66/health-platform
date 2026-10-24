<script setup>
//导入图标
import { BellFilled, Position, EditPen, TrendCharts, Tickets, Clock } from '@element-plus/icons-vue'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
//导入患者端接口
import { getProfileService, getMyRequestsService, getHealthRecordsService, sendSosService, updateProfileService, reportLocationService } from '@/api/patient'
//导入高德地图工具（定位上报）
import { loadAMap, getMyLocation } from '@/utils/amap'
import { useUserStore } from '@/stores/user'
import { REQUEST_STATUS } from '@/utils/constants'

const router = useRouter()
const userStore = useUserStore()

const profile = ref(null)
const requests = ref([])
const healthRecords = ref([])
const loading = ref(false)
const sosLoading = ref(false)

//档案编辑弹窗
const profileVisible = ref(false)
const profileSaving = ref(false)
const profileForm = ref({})

const pendingCount = computed(() => requests.value.filter((r) => [0, 1, 2].includes(r.status)).length)
const doneCount = computed(() => requests.value.filter((r) => r.status === 3).length)
const healthCount = computed(() => healthRecords.value.length)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

/** 根据出生日期计算年龄 */
const age = computed(() => {
  const birthday = profile.value?.birthday
  if (!birthday) return null
  const diff = Date.now() - new Date(birthday).getTime()
  return Math.floor(diff / (365.25 * 24 * 3600 * 1000))
})

/** 居住位置展示：楼栋-楼层-房间 */
const locationText = computed(() => {
  if (!profile.value) return ''
  const parts = [profile.value.building, profile.value.floor, profile.value.room].filter(Boolean)
  return parts.join(' ')
})

const loadAll = async () => {
  loading.value = true
  try {
    //看板需要全量数据做统计，两个列表接口一次性拉取较大分页（个人数据量级，1000 条足够覆盖）
    const [p, r, h] = await Promise.allSettled([
      getProfileService(),
      getMyRequestsService({ pageNum: 1, pageSize: 1000 }),
      getHealthRecordsService({ pageNum: 1, pageSize: 1000 }),
    ])
    if (p.status === 'fulfilled') profile.value = p.value
    if (r.status === 'fulfilled') requests.value = r.value?.records || []
    if (h.status === 'fulfilled') healthRecords.value = h.value?.records || []
  } finally {
    loading.value = false
    //有服务中的需求时启动位置心跳（无则停止，保护隐私）
    stopLocationReport()
    if (hasDoingRequest.value) startLocationReport()
  }
}

/** 判断是否有服务中(状态2)的需求 */
const hasDoingRequest = computed(() => requests.value.some((r) => r.status === 2))

//位置心跳定时器（服务中每5秒上报一次，供志愿者查看实时位置）
let locationTimer = null

const startLocationReport = async () => {
  try {
    const AMap = await loadAMap()
    const reportOnce = async () => {
      const loc = await getMyLocation(AMap)
      if (loc) await reportLocationService(loc).catch(() => {})
    }
    reportOnce() // 立即上报一次
    locationTimer = setInterval(reportOnce, 5000) // 之后每5秒上报
  } catch {
    /* 地图加载失败不影响页面其他功能 */
  }
}

const stopLocationReport = () => {
  if (locationTimer) {
    clearInterval(locationTimer)
    locationTimer = null
  }
}

//离开页面时停止上报
onUnmounted(stopLocationReport)

/** 获取当前位置（正式版应使用高德地图 JS API 精确定位） */
const getLocation = () =>
  new Promise((resolve) => {
    if (!navigator.geolocation) return resolve(null)
    navigator.geolocation.getCurrentPosition(
      (pos) => resolve({ longitude: pos.coords.longitude, latitude: pos.coords.latitude }),
      () => resolve(null),
      { timeout: 5000 },
    )
  })

/** SOS 一键求助 */
const handleSos = async () => {
  try {
    await ElMessageBox.confirm(
      '即将向平台发送紧急求助，管理员与院内志愿者将收到通知并尽快赶来，是否确认？',
      'SOS 紧急求助',
      { confirmButtonText: '确认求助', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'sos-confirm-btn' },
    )
  } catch {
    return
  }
  sosLoading.value = true
  try {
    const location = await getLocation()
    if (!location) {
      ElMessage.error('定位失败，请允许浏览器获取位置后重试')
      return
    }
    await sendSosService({ longitude: location.longitude, latitude: location.latitude })
    ElMessage.success('求助已发出，请保持电话畅通，工作人员将尽快与您联系！')
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    sosLoading.value = false
  }
}

/** 打开档案编辑弹窗 */
const openProfile = async () => {
  try {
    const data = profile.value || (await getProfileService())
    profileForm.value = {
      idCardNo: data?.idCardNo || '',
      birthday: data?.birthday || '',
      gender: data?.gender ?? null,
      emergencyContactName: data?.emergencyContactName || '',
      emergencyContactPhone: data?.emergencyContactPhone || '',
      homeAddress: data?.homeAddress || '',
      allergyHistory: data?.allergyHistory || '',
      chronicDiseases: data?.chronicDiseases || '',
      medicationList: data?.medicationList || '',
      building: data?.building || '',
      floor: data?.floor || '',
      room: data?.room || '',
    }
    profileVisible.value = true
  } catch {
    // 失败提示已由请求拦截器统一弹出
  }
}

/** 保存档案 */
const saveProfile = async () => {
  profileSaving.value = true
  try {
    await updateProfileService(profileForm.value)
    ElMessage.success('档案保存成功')
    profileVisible.value = false
    loadAll()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    profileSaving.value = false
  }
}

onMounted(loadAll)
</script>

<template>
  <div v-loading="loading">
    <!-- 欢迎卡片 -->
    <div class="page-card welcome-card">
      <div class="welcome-info">
        <el-avatar :size="64" class="welcome-avatar">{{ (userStore.displayName || '患')[0] }}</el-avatar>
        <div>
          <div class="welcome-title">{{ greeting }}，{{ profile?.realName || userStore.displayName }}</div>
          <div class="welcome-sub">
            <template v-if="profile && (age != null || locationText)">
              {{ age != null ? `${age} 岁` : '' }}
              {{ age != null && profile.gender != null ? ' · ' : '' }}
              {{ profile.gender === 1 ? '男' : profile.gender === 0 ? '女' : '' }}
              <span v-if="locationText"> · {{ locationText }}</span>
            </template>
            <template v-else>尚未完善健康档案</template>
            <el-button size="small" type="primary" plain style="margin-left: 10px" @click="openProfile">编辑档案</el-button>
          </div>
        </div>
      </div>

      <!-- SOS 按钮 -->
      <div class="sos-area">
        <button class="sos-btn" :disabled="sosLoading" @click="handleSos">
          <el-icon class="sos-icon"><BellFilled /></el-icon>
          <span>{{ sosLoading ? '求助中...' : 'SOS' }}</span>
        </button>
        <div class="sos-tip">遇到紧急情况，请立即按下</div>
      </div>
    </div>

    <!-- 数据概览 -->
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="page-card stat-card" @click="router.push('/patient/requests')">
          <div class="stat-num">{{ pendingCount }}</div>
          <div class="stat-label">进行中的需求</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="page-card stat-card" @click="router.push('/patient/requests')">
          <div class="stat-num">{{ doneCount }}</div>
          <div class="stat-label">已完成的需求</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="page-card stat-card" @click="router.push('/patient/health-report')">
          <div class="stat-num">{{ healthCount }}</div>
          <div class="stat-label">健康记录条数</div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷入口 + 最近需求 -->
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="page-card">
          <h3 class="page-title"><el-icon><Position /></el-icon>快捷入口</h3>
          <div class="quick-links">
            <el-button type="primary" plain @click="router.push('/patient/publish-request')">
              <el-icon><EditPen /></el-icon>发布服务需求
            </el-button>
            <el-button type="success" plain @click="router.push('/patient/health-report')">
              <el-icon><TrendCharts /></el-icon>上报健康数据
            </el-button>
            <el-button plain @click="router.push('/patient/requests')">
              <el-icon><Tickets /></el-icon>查看我的需求
            </el-button>
          </div>
        </div>
      </el-col>
      <el-col :span="16">
        <div class="page-card">
          <h3 class="page-title"><el-icon><Clock /></el-icon>最近需求</h3>
          <el-table :data="requests.slice(0, 5)" size="small">
            <el-table-column label="服务类型" min-width="100">
              <template #default="{ row }">{{ row.category_name || row.categoryId || '-' }}</template>
            </el-table-column>
            <el-table-column prop="appointment_time" label="约定时间" min-width="150" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="REQUEST_STATUS[row.status]?.type || 'info'" size="small">
                  {{ REQUEST_STATUS[row.status]?.text || `状态${row.status}` }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!requests.length" description="还没有发布过需求" :image-size="70" />
        </div>
      </el-col>
    </el-row>

    <!-- 档案编辑弹窗 -->
    <el-dialog v-model="profileVisible" title="完善健康档案" width="620px">
      <el-form label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker
                v-model="profileForm.birthday"
                type="date"
                placeholder="选择出生日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="profileForm.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="身份证号">
          <el-input v-model="profileForm.idCardNo" placeholder="仅管理员可见（敏感字段）" maxlength="18" />
        </el-form-item>
        <el-form-item label="家庭住址">
          <el-input v-model="profileForm.homeAddress" placeholder="例如：幸福养老院 3 号楼 205 房间" maxlength="100" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="楼栋">
              <el-input v-model="profileForm.building" placeholder="如：3号楼" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="楼层">
              <el-input v-model="profileForm.floor" placeholder="如：2层" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="房间号">
              <el-input v-model="profileForm.room" placeholder="如：205" maxlength="20" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="紧急联系人">
              <el-input v-model="profileForm.emergencyContactName" placeholder="姓名" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人电话">
              <el-input v-model="profileForm.emergencyContactPhone" placeholder="手机号" maxlength="20" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="过敏史">
          <el-input v-model="profileForm.allergyHistory" placeholder="无则填：无" maxlength="100" />
        </el-form-item>
        <el-form-item label="慢性病史">
          <el-input v-model="profileForm.chronicDiseases" placeholder="例如：高血压、糖尿病（仅管理员可见）" maxlength="100" />
        </el-form-item>
        <el-form-item label="服药清单">
          <el-input v-model="profileForm.medicationList" type="textarea" :rows="2" placeholder="例如：氨氯地平片每日一次（仅管理员可见）" maxlength="300" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存档案</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.welcome-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #f0f8fc 0%, #e5f1f9 100%);
}

.welcome-info {
  display: flex;
  align-items: center;
  gap: 16px;

  .welcome-avatar {
    background: var(--el-color-primary);
    color: #fff;
    font-size: 26px;
  }

  .welcome-title {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 6px;
  }

  .welcome-sub {
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.sos-area {
  text-align: center;
}

.sos-btn {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  border: 4px solid #f6c9c8;
  background: radial-gradient(circle, #ff6b67 0%, var(--sos-color) 100%);
  color: #fff;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 2px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  transition: transform 0.15s, box-shadow 0.15s;
  box-shadow: 0 6px 18px rgba(230, 67, 64, 0.4);

  &:hover {
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.96);
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.7;
  }

  .sos-icon {
    font-size: 24px;
  }
}

.sos-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 14px rgba(48, 155, 204, 0.2);
  }

  .stat-num {
    font-size: 32px;
    font-weight: 700;
    color: var(--el-color-primary);
  }

  .stat-label {
    margin-top: 6px;
    font-size: 13px;
    color: var(--text-secondary);
  }
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .el-button {
    width: 100%;
    margin-left: 0;
    justify-content: flex-start;
  }
}
</style>
