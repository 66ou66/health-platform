<script setup>
//导入图标
import { TrendCharts, Clock } from '@element-plus/icons-vue'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
//导入患者端接口
import { getHealthRecordsService, addHealthRecordService } from '@/api/patient'
import { MOOD_OPTIONS } from '@/utils/constants'
//分页条组件
import ListPagination from '@/components/ListPagination.vue'

const records = ref([])
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// HealthRecord 实体字段以后端为准
const form = reactive({
  bloodPressureSys: null, // 收缩压（高压）
  bloodPressureDia: null, // 舒张压（低压）
  heartRate: null, // 心率
  bloodSugar: null, // 血糖
  moodStatus: '', // 心情状态
  recordTime: '',
  note: '',
})

const rules = {
  bloodPressureSys: [{ type: 'number', min: 50, max: 260, message: '收缩压范围 50-260 mmHg', trigger: 'blur' }],
  bloodPressureDia: [{ type: 'number', min: 30, max: 160, message: '舒张压范围 30-160 mmHg', trigger: 'blur' }],
  heartRate: [{ type: 'number', min: 20, max: 220, message: '心率范围 20-220 次/分', trigger: 'blur' }],
  bloodSugar: [{ type: 'number', min: 1, max: 40, message: '血糖范围 1-40 mmol/L', trigger: 'blur' }],
}

const loadRecords = async () => {
  loading.value = true
  try {
    const data = (await getHealthRecordsService({ pageNum: pageNum.value, pageSize: pageSize.value })) || {}
    records.value = data.records || []
    total.value = data.total || 0
    //当前页被删空时自动回退一页
    if (!records.value.length && pageNum.value > 1) {
      pageNum.value -= 1
      return loadRecords()
    }
  } finally {
    loading.value = false
  }
}

const onPageChange = (page, size) => {
  pageNum.value = page
  pageSize.value = size
  loadRecords()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  //至少填写一项指标
  if (!form.bloodPressureSys && !form.bloodPressureDia && !form.heartRate && !form.bloodSugar && !form.moodStatus) {
    ElMessage.warning('请至少填写一项健康指标')
    return
  }
  submitting.value = true
  try {
    await addHealthRecordService({ ...form })
    ElMessage.success('健康数据上报成功')
    formRef.value.resetFields()
    form.recordTime = ''
    form.moodStatus = ''
    loadRecords()
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    submitting.value = false
  }
}

/** 最近一条记录的健康摘要 */
const latestSummary = (row) => {
  const parts = []
  if (row.bloodPressureSys && row.bloodPressureDia) parts.push(`血压 ${row.bloodPressureSys}/${row.bloodPressureDia}`)
  if (row.heartRate) parts.push(`心率 ${row.heartRate}`)
  if (row.bloodSugar) parts.push(`血糖 ${row.bloodSugar}`)
  if (row.moodStatus) parts.push(`心情 ${row.moodStatus}`)
  return parts.join('  ·  ') || '仅备注'
}

onMounted(loadRecords)
</script>

<template>
  <el-row :gutter="16">
    <!-- 上报表单 -->
    <el-col :span="10">
      <div class="page-card">
        <h3 class="page-title"><el-icon><TrendCharts /></el-icon>上报健康数据</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="收缩压" prop="bloodPressureSys">
            <el-input-number v-model="form.bloodPressureSys" :min="50" :max="260" placeholder="mmHg" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="舒张压" prop="bloodPressureDia">
            <el-input-number v-model="form.bloodPressureDia" :min="30" :max="160" placeholder="mmHg" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="心率" prop="heartRate">
            <el-input-number v-model="form.heartRate" :min="20" :max="220" placeholder="次/分" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="血糖" prop="bloodSugar">
            <el-input-number v-model="form.bloodSugar" :min="1" :max="40" :step="0.1" :precision="1" placeholder="mmol/L" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="心情状态">
            <el-select v-model="form.moodStatus" placeholder="今天心情如何？（选填）" clearable style="width: 100%">
              <el-option v-for="m in MOOD_OPTIONS" :key="m" :label="m" :value="m" />
            </el-select>
          </el-form-item>
          <el-form-item label="测量时间" prop="recordTime">
            <el-date-picker
              v-model="form.recordTime"
              type="datetime"
              placeholder="默认当前时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.note" type="textarea" :rows="2" maxlength="200" placeholder="身体感受、用药情况等（选填）" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交上报</el-button>
            <el-button @click="formRef.resetFields()">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-col>

    <!-- 历史记录 -->
    <el-col :span="14">
      <div class="page-card">
        <h3 class="page-title"><el-icon><Clock /></el-icon>健康记录</h3>
        <el-table v-loading="loading" :data="records" size="small" max-height="560">
          <el-table-column prop="recordTime" label="时间" min-width="150" />
          <el-table-column label="数据摘要" min-width="220">
            <template #default="{ row }">{{ latestSummary(row) }}</template>
          </el-table-column>
          <el-table-column prop="note" label="备注" min-width="140" show-overflow-tooltip />
        </el-table>
        <el-empty v-if="!loading && !records.length" description="暂无健康记录" />

        <ListPagination :total="total" :page="pageNum" :page-size="pageSize" @change="onPageChange" />
      </div>
    </el-col>
  </el-row>
</template>
