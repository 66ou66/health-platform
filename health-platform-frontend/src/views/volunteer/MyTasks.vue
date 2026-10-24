<script setup>
//导入图标
import { List, Refresh } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
//导入志愿者端接口
import { getMyTasksService, getTaskDetailService, signInService, signOutService, submitFeedbackService, getProfileService, updateProfileService, getPatientLocationService } from '@/api/volunteer'
//导入高德地图工具（实时地图）
import { loadAMap, getMyLocation, distanceMeters } from '@/utils/amap'
import { TASK_STATUS, CERT_STATUS } from '@/utils/constants'
//分页条组件
import ListPagination from '@/components/ListPagination.vue'

const tasks = ref([])
const loading = ref(false)
const operating = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

//详情弹窗
const detailVisible = ref(false)
const detail = ref(null)
const detailLoading = ref(false)

//签到弹窗
const signVisible = ref(false)
const signForm = ref({ address: '', longitude: null, latitude: null })
const signType = ref('in') // in=签到 out=签退
const signRow = ref(null)

//评价弹窗（评价患者）
const evaluateVisible = ref(false)
const evaluateRow = ref(null)
const evaluating = ref(false)
const rating = ref(5)
const evaluateContent = ref('')
const evaluatePublic = ref(true)

//档案弹窗（实名认证与技能标签）
const profileVisible = ref(false)
const profileLoading = ref(false)
const savingProfile = ref(false)
const profile = ref(null)
const profileForm = ref({ idCardNo: '', skillTags: [], serviceRegion: '', dutyArea: '' })

const statusOf = (row) => TASK_STATUS[row.status] || { text: `状态${row.status}`, type: 'info' }
const certOf = (row) => CERT_STATUS[row.certStatus] || { text: `状态${row.certStatus}`, type: 'info' }

const loadTasks = async () => {
  loading.value = true
  try {
    const data = (await getMyTasksService({ pageNum: pageNum.value, pageSize: pageSize.value })) || {}
    tasks.value = data.records || []
    total.value = data.total || 0
    //当前页被删空时自动回退一页
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

/** 查看任务详情（含患者脱敏信息，对应 TaskDetailRespVo） */
const showDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await getTaskDetailService(row.id ?? row.taskId)
  } finally {
    detailLoading.value = false
  }
}

/** 打开签到/签退弹窗（高德定位：GPS 不可用时自动回退 IP 定位，几乎不会失败） */
const openSign = async (row, type) => {
  signType.value = type
  signRow.value = row
  signForm.value = { address: '', longitude: null, latitude: null }
  try {
    const AMap = await loadAMap()
    const location = await getMyLocation(AMap)
    if (location) {
      signForm.value.longitude = location.longitude
      signForm.value.latitude = location.latitude
    }
  } catch {
    /* 地图加载失败时仍可签到，仅无坐标记录 */
  }
  signVisible.value = true
}

/** 确认签到/签退（SignInReqVo 字段以后端为准） */
const confirmSign = async () => {
  operating.value = true
  try {
    const body = {
      longitude: signForm.value.longitude,
      latitude: signForm.value.latitude,
      address: signForm.value.address,
    }
    if (signType.value === 'in') {
      await signInService(signRow.value.id ?? signRow.value.taskId, body)
      ElMessage.success('签到成功，服务开始！')
    } else {
      await signOutService(signRow.value.id ?? signRow.value.taskId, body)
      ElMessage.success('签退成功，辛苦了！')
    }
    signVisible.value = false
    loadTasks()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    operating.value = false
  }
}

/** 打开评价弹窗 */
const openEvaluate = (row) => {
  evaluateRow.value = row
  rating.value = 5
  evaluateContent.value = ''
  evaluatePublic.value = true
  evaluateVisible.value = true
}

/** 提交评价（评价患者） */
const submitEvaluate = async () => {
  evaluating.value = true
  try {
    await submitFeedbackService({
      taskId: evaluateRow.value.id ?? evaluateRow.value.taskId,
      rating: rating.value,
      content: evaluateContent.value,
      isPublic: evaluatePublic.value ? 1 : 0,
    })
    ElMessage.success('评价提交成功，感谢您的反馈！')
    evaluateVisible.value = false
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    evaluating.value = false
  }
}

/** 打开档案弹窗（实名认证与技能标签） */
const openProfile = async () => {
  profileVisible.value = true
  profileLoading.value = true
  try {
    const data = await getProfileService()
    profile.value = data || null
    profileForm.value = {
      idCardNo: data?.idCardNo || '',
      skillTags: data?.skillTags || [],
      serviceRegion: data?.serviceRegion || '',
      dutyArea: data?.dutyArea || '',
    }
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    profileLoading.value = false
  }
}

/** 保存档案（提交后重新进入待审核状态） */
const saveProfile = async () => {
  savingProfile.value = true
  try {
    await updateProfileService(profileForm.value)
    ElMessage.success('档案已提交，等待管理员审核')
    profileVisible.value = false
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    savingProfile.value = false
  }
}

/** 任务状态流转：0已接单 → 签到 → 1服务中 → 签退 → 2已完成 */
const canSignIn = (row) => row.status === 0 && !row.signInTime
const canSignOut = (row) => [0, 1].includes(row.status) && !!row.signInTime && !row.signOutTime
const canEvaluate = (row) => row.status === 2

// ================= 实时地图弹窗（打车式：双方位置 + 距离） =================
const trackVisible = ref(false)
const trackRow = ref(null)
const trackDistance = ref('')
let trackMap = null
let trackMeMarker = null
let trackPatientMarker = null
let trackLine = null
let trackTimer = null

/** 打开实时地图（仅服务中状态可点） */
const openTrack = async (row) => {
  trackRow.value = row
  trackVisible.value = true
  trackDistance.value = '定位中...'
  try {
    const AMap = await loadAMap()
    //等弹窗 DOM 渲染完成
    await new Promise((r) => setTimeout(r, 100))
    trackMap = new AMap.Map('amap-track', { zoom: 15 })
    //自己的位置（志愿者本地定位，无需存库）
    const myLoc = await getMyLocation(AMap)
    if (myLoc) {
      trackMap.setCenter([myLoc.longitude, myLoc.latitude])
      trackMeMarker = new AMap.Marker({ position: [myLoc.longitude, myLoc.latitude], title: '我的位置' })
      trackMap.add(trackMeMarker)
    }
    //患者位置（先占位，拿到心跳数据后更新）
    trackPatientMarker = new AMap.Marker({ position: [0, 0], title: '患者位置' })
    trackLine = new AMap.Polyline({ path: [[0, 0], [0, 0]], strokeColor: '#e64340', strokeWeight: 3 })
    trackMap.add([trackPatientMarker, trackLine])
    refreshTrack(AMap)
    //每5秒刷新患者位置（对应报告轮询方案）
    trackTimer = setInterval(() => refreshTrack(AMap), 5000)
  } catch (e) {
    //把真实原因打到控制台，方便定位是白名单还是Key问题
    console.error('实时地图加载失败：', e)
    ElMessage.error(`地图加载失败：${e?.message || '请检查高德Key与域名白名单'}`)
    trackVisible.value = false
  }
}

/** 刷新患者位置与距离 */
const refreshTrack = async (AMap) => {
  const data = await getPatientLocationService(trackRow.value.id).catch(() => null)
  if (!data || !trackPatientMarker) return
  const pLng = Number(data.longitude)
  const pLat = Number(data.latitude)
  trackPatientMarker.setPosition([pLng, pLat])
  //自己位置没拿到时只更新患者点
  const mePos = trackMeMarker && trackMeMarker.getPosition()
  if (mePos) {
    trackLine.setPath([[mePos.lng, mePos.lat], [pLng, pLat]])
    const meters = distanceMeters(AMap, [mePos.lng, mePos.lat], [pLng, pLat])
    trackDistance.value = meters < 1000
      ? `${Math.round(meters)} 米`
      : `${(meters / 1000).toFixed(1)} 公里`
  }
}

/** 关闭弹窗时清理地图和定时器（防止内存泄漏） */
const closeTrack = () => {
  trackVisible.value = false
  if (trackTimer) { clearInterval(trackTimer); trackTimer = null }
  if (trackMap) { trackMap.destroy(); trackMap = null }
  trackMeMarker = null
  trackPatientMarker = null
  trackLine = null
}

onMounted(loadTasks)
</script>

<template>
  <div class="page-card">
    <h3 class="page-title">
      <el-icon><List /></el-icon>我的任务
      <el-button type="primary" size="small" @click="openProfile">完善档案</el-button>
      <el-button size="small" :icon="Refresh" circle @click="loadTasks" />
    </h3>

    <el-table v-loading="loading" :data="tasks">
      <el-table-column prop="id" label="任务编号" width="90" />
      <el-table-column label="服务类型" min-width="100">
        <template #default="{ row }">{{ row.category_name || row.categoryId || '-' }}</template>
      </el-table-column>
      <el-table-column label="需求" min-width="160">
        <template #default="{ row }">
          <el-tag v-if="row.emergency_flag === 1" type="danger" size="small">紧急</el-tag>
          {{ row.title || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="患者" min-width="90">
        <template #default="{ row }">{{ row.patient_name || '-' }}</template>
      </el-table-column>
      <el-table-column prop="appointment_time" label="约定时间" min-width="160" />
      <el-table-column label="签到时间" min-width="160">
        <template #default="{ row }">{{ row.sign_in_time || '—' }}</template>
      </el-table-column>
      <el-table-column label="签退时间" min-width="160">
        <template #default="{ row }">{{ row.sign_out_time || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusOf(row).type" size="small">{{ statusOf(row).text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="showDetail(row)">详情</el-button>
          <el-button v-if="canSignIn(row)" size="small" link type="warning" @click="openSign(row, 'in')">签到</el-button>
          <el-button v-if="canSignOut(row)" size="small" link type="success" @click="openSign(row, 'out')">签退</el-button>
          <el-button v-if="row.status === 1" size="small" link type="primary" @click="openTrack(row)">实时地图</el-button>
          <el-button v-if="canEvaluate(row)" size="small" link type="primary" @click="openEvaluate(row)">评价</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !tasks.length" description="还没有接过任务，去任务大厅看看吧" />

    <ListPagination :total="total" :page="pageNum" :page-size="pageSize" @change="onPageChange" />
  </div>

  <!-- 任务详情弹窗 -->
  <el-dialog v-model="detailVisible" title="任务详情" width="520px" v-loading="detailLoading">
    <template v-if="detail">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="服务类型">{{ detail.categoryName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="需求标题">{{ detail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="约定时间">{{ detail.appointmentTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务地址">{{ detail.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="需求描述">{{ detail.description || '—' }}</el-descriptions-item>
        <el-descriptions-item label="患者姓名">{{ detail.patientName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="过敏史">{{ detail.allergyHistory || '无' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人">
          {{ detail.emergencyContactName || '—' }} {{ detail.emergencyContactPhone || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="签到时间">{{ detail.signInTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="签退时间">{{ detail.signOutTime || '—' }}</el-descriptions-item>
      </el-descriptions>
    </template>
    <el-empty v-else-if="!detailLoading" description="暂无详情数据" />
  </el-dialog>

  <!-- 签到/签退弹窗 -->
  <el-dialog v-model="signVisible" :title="signType === 'in' ? '服务签到' : '服务签退'" width="420px">
    <el-alert
      :title="signType === 'in' ? '签到表示已到达服务地点并开始服务' : '签退表示服务已完成'"
      type="info"
      :closable="false"
      show-icon
      class="sign-alert"
    />
    <el-form label-width="80px">
      <el-form-item label="当前定位">
        <el-tag v-if="signForm.longitude != null" type="success" size="small">
          {{ Number(signForm.longitude).toFixed(5) }}, {{ Number(signForm.latitude).toFixed(5) }}
        </el-tag>
        <el-tag v-else type="warning" size="small">定位失败（仍可签到，无坐标记录）</el-tag>
      </el-form-item>
      <el-form-item label="位置说明">
        <el-input v-model="signForm.address" placeholder="例如：幸福养老院 3 号楼 205 房间（选填）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="signVisible = false">取消</el-button>
      <el-button type="primary" :loading="operating" @click="confirmSign">
        {{ signType === 'in' ? '确认签到' : '确认签退' }}
      </el-button>
    </template>
  </el-dialog>

  <!-- 评价患者弹窗 -->
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

  <!-- 实时地图弹窗（打车式：双方位置 + 距离，每5秒刷新） -->
  <el-dialog v-model="trackVisible" title="实时位置" width="600px" @closed="closeTrack">
    <div class="track-tip">
      距离患者：<strong>{{ trackDistance }}</strong>
      <span class="track-sub">每 5 秒自动刷新</span>
    </div>
    <div id="amap-track" style="width: 100%; height: 400px"></div>
  </el-dialog>

  <!-- 志愿者档案弹窗（实名认证 + 技能标签 + 荣誉数据） -->
  <el-dialog v-model="profileVisible" title="我的档案" width="500px" v-loading="profileLoading">
    <el-descriptions v-if="profile" :column="2" border class="honor-desc">
      <el-descriptions-item label="认证状态">
        <el-tag :type="certOf(profile).type" size="small">{{ certOf(profile).text }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="星星总数">{{ profile.starsTotal ?? 0 }} ⭐</el-descriptions-item>
      <el-descriptions-item label="累计服务时长">{{ profile.totalServiceHours ?? 0 }} 小时</el-descriptions-item>
      <el-descriptions-item label="平均评分">{{ profile.ratingScore ?? '暂无评分' }}</el-descriptions-item>
    </el-descriptions>
    <el-alert
      v-if="profile?.certifyReason"
      :title="`上次驳回原因：${profile.certifyReason}`"
      type="warning"
      :closable="false"
      show-icon
      class="sign-alert"
    />
    <el-form label-width="80px">
      <el-form-item label="身份证号" required>
        <el-input v-model="profileForm.idCardNo" placeholder="实名认证必填" maxlength="18" />
      </el-form-item>
      <el-form-item label="技能标签">
        <el-select
          v-model="profileForm.skillTags"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入技能标签，例如：陪诊"
          style="width: 100%"
        >
          <el-option v-for="s in ['陪诊', '买药', '照料', '陪伴', '家政']" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item label="服务区域">
        <el-input v-model="profileForm.serviceRegion" placeholder="例如：幸福养老院（选填）" maxlength="50" />
      </el-form-item>
      <el-form-item label="值班区域">
        <el-input v-model="profileForm.dutyArea" placeholder="例如：3号楼（选填）" maxlength="50" />
      </el-form-item>
      <div class="field-tip">修改档案后将重新进入待审核状态</div>
    </el-form>
    <template #footer>
      <el-button @click="profileVisible = false">取消</el-button>
      <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存</el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.sign-alert {
  margin-bottom: 16px;
}

.honor-desc {
  margin-bottom: 16px;
}

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

.field-tip {
  font-size: 12px;
  color: var(--text-secondary);
}

.track-tip {
  margin-bottom: 10px;
  font-size: 15px;

  strong {
    color: var(--sos-color);
    font-size: 18px;
  }

  .track-sub {
    margin-left: 12px;
    font-size: 12px;
    color: var(--text-secondary);
  }
}
</style>
