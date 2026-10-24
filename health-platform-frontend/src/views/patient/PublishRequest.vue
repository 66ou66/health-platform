<script setup>
//导入图标
import { EditPen } from '@element-plus/icons-vue'
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
//导入患者端接口
import { publishRequestService, getProfileService, getCategoriesService } from '@/api/patient'
import { DEFAULT_CATEGORIES } from '@/utils/constants'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

// PublishRequestReqVo 字段以后端为准
const form = reactive({
  categoryId: null,
  title: '',
  description: '',
  address: '',
  appointmentTime: '',
  emergencyFlag: 0,
})

const rules = {
  categoryId: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  title: [{ required: true, message: '请填写需求标题', trigger: 'blur' }],
  appointmentTime: [{ required: true, message: '请选择约定服务时间', trigger: 'change' }],
  address: [{ required: true, message: '请填写服务地址', trigger: 'blur' }],
}

//服务分类列表（从后端获取，失败时用默认分类兜底）
const categories = ref(DEFAULT_CATEGORIES)

/** 默认使用档案中的住址，省去重复填写 */
onMounted(async () => {
  try {
    const [profile, list] = await Promise.all([getProfileService(), getCategoriesService()])
    if (profile?.homeAddress) form.address = profile.homeAddress
    if (list?.length) categories.value = list
  } catch {
    /* 获取失败不阻塞发布流程，使用默认兜底 */
  }
})

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await publishRequestService({ ...form })
    ElMessage.success('需求发布成功，等待志愿者接单')
    router.push('/patient/requests')
  } catch {
    // 失败提示已由请求拦截器统一弹出
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page-card publish-card">
    <h3 class="page-title"><el-icon><EditPen /></el-icon>发布服务需求</h3>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="publish-form">
      <el-form-item label="服务类型" prop="categoryId">
        <el-radio-group v-model="form.categoryId">
          <el-radio-button v-for="c in categories" :key="c.id" :value="c.id">
            {{ c.categoryName }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="需求标题" prop="title">
        <el-input v-model="form.title" placeholder="一句话概括需求，例如：陪同去市一院复诊" maxlength="100" show-word-limit />
      </el-form-item>

      <el-form-item label="约定时间" prop="appointmentTime">
        <el-date-picker
          v-model="form.appointmentTime"
          type="datetime"
          placeholder="选择约定服务时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          :disabled-date="(d) => d.getTime() < Date.now() - 86400000"
        />
      </el-form-item>

      <el-form-item label="服务地址" prop="address">
        <el-input v-model="form.address" placeholder="例如：幸福养老院 3 号楼 205 房间" maxlength="100" show-word-limit />
      </el-form-item>

      <el-form-item label="是否紧急">
        <el-switch
          v-model="form.emergencyFlag"
          :active-value="1"
          :inactive-value="0"
          active-text="紧急需求（任务大厅置顶）"
        />
      </el-form-item>

      <el-form-item label="需求描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          placeholder="请详细描述您的需求，例如：明天上午需要去市第一人民医院复诊，希望志愿者陪同挂号、缴费、取药"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">发布需求</el-button>
        <el-button @click="router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style lang="scss" scoped>
.publish-card {
  max-width: 720px;
}
</style>
