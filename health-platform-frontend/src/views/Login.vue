<script setup>
//导入图标
import { User, Lock } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
//导入登录、注册接口
import { loginService, registerService, checkUsernameService, testConnectionService } from '@/api/user'
import { useUserStore, ROLE_HOME } from '@/stores/user'
import { ROLE, ROLE_TEXT } from '@/utils/constants'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

//控制注册与登录表单的显示，默认显示登录
const isRegister = ref(false)
const formRef = ref()
const loading = ref(false)
const testing = ref(false)

//角色列表（数值与数据库 users.role 一致：0管理员 1患者 2志愿者）
const roles = [
    { value: ROLE.ADMIN, label: ROLE_TEXT[ROLE.ADMIN] },
    { value: ROLE.PATIENT, label: ROLE_TEXT[ROLE.PATIENT] },
    { value: ROLE.VOLUNTEER, label: ROLE_TEXT[ROLE.VOLUNTEER] },
]

//定义登录相关的数据
const loginData = ref({
    userName: '',
    password: '',
    role: ROLE.PATIENT,
})

//定义注册相关的数据
const registerData = ref({
    userName: '',
    password: '',
    rePassword: '',
    realName: '',
    phone: '',
    role: ROLE.PATIENT,
})

//注册表单校验规则（确认密码必须与密码一致）
const rePasswordValidator = (_rule, value, callback) => {
    if (value !== registerData.value.password) callback(new Error('两次输入的密码不一致'))
    else callback()
}

const registerRules = {
    userName: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度 3-20 位', trigger: 'blur' },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' },
    ],
    rePassword: [{ validator: rePasswordValidator, trigger: 'blur' }],
    role: [{ required: true, message: '请选择角色', trigger: 'change' }],
    phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
}

//检查用户名是否已存在
const checkUsername = async () => {
    if (!registerData.value.userName || registerData.value.userName.length < 3) return
    try {
        const exists = await checkUsernameService(registerData.value.userName)
        //返回值的含义以后端为准（true 表示已存在）
        if (exists) ElMessage.warning('该用户名已被注册')
    } catch {
        /* 接口失败时忽略，注册时后端会再次校验 */
    }
}

//登录的方法
const login = async () => {
    loading.value = true
    try {
        //LoginRespVo 字段以后端为准，这里兼容几种常见返回结构
        const data = await loginService(loginData.value)
        const token = data?.token || data?.jwt || ''
        if (!token) {
            ElMessage.error('登录返回数据中没有 token，请检查后端 LoginRespVo 字段')
            return
        }
        const userInfo = data?.userInfo || data?.user || null
        if (userInfo && !userInfo.role) userInfo.role = loginData.value.role
        //保存登录状态
        userStore.setLogin(token, userInfo || { userName: loginData.value.userName, role: loginData.value.role })
        ElMessage.success('登陆成功')
        //跳转到自己角色的首页（若登录前想访问其他页面则优先跳转）
        const home = ROLE_HOME[userStore.role] || '/'
        router.push(route.query.redirect || home)
    } catch {
        //失败提示已由请求拦截器统一弹出
    } finally {
        loading.value = false
    }
}

//注册的方法
const register = async () => {
    await formRef.value.validate()
    loading.value = true
    try {
        //rePassword 为纯前端校验字段，不提交给后端
        const { rePassword, ...payload } = registerData.value
        await registerService(payload)
        ElMessage.success('注册成功，请登录')
        //注册成功后自动填充登录表单并切回登录
        loginData.value.userName = registerData.value.userName
        loginData.value.role = registerData.value.role
        isRegister.value = false
    } catch {
        //失败提示已由请求拦截器统一弹出
    } finally {
        loading.value = false
    }
}

//测试前后端连接
const testConnection = async () => {
    testing.value = true
    try {
        const res = await testConnectionService()
        ElMessage.success(`后端连接正常：${JSON.stringify(res)}`)
    } catch {
        ElMessage.error('后端连接失败，请确认后端已在 8080 端口启动')
    } finally {
        testing.value = false
    }
}
</script>

<template>
    <el-row class="login-page">
        <!-- 左侧背景图 -->
        <el-col :span="12" class="bg"></el-col>
        <!-- 右侧表单 -->
        <el-col :span="6" :offset="3" class="form">
            <!-- 注册表单 -->
            <el-form ref="formRef" :model="registerData" :rules="registerRules" size="large" autocomplete="off" v-if="isRegister">
                <el-form-item>
                    <h1>注册</h1>
                </el-form-item>
                <el-form-item prop="userName">
                    <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="registerData.userName" @blur="checkUsername"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input :prefix-icon="Lock" type="password" placeholder="请输入密码" v-model="registerData.password"></el-input>
                </el-form-item>
                <el-form-item prop="rePassword">
                    <el-input :prefix-icon="Lock" type="password" placeholder="请输入再次密码" v-model="registerData.rePassword"></el-input>
                </el-form-item>
                <el-form-item prop="realName">
                    <el-input :prefix-icon="User" placeholder="请输入真实姓名（选填）" v-model="registerData.realName"></el-input>
                </el-form-item>
                <el-form-item prop="phone">
                    <el-input :prefix-icon="User" placeholder="请输入手机号（选填）" v-model="registerData.phone"></el-input>
                </el-form-item>
                <el-form-item prop="role">
                    <el-select placeholder="请选择角色" v-model="registerData.role">
                        <el-option v-for="role in roles" :key="role.value" :label="role.label" :value="role.value"></el-option>
                    </el-select>
                </el-form-item>
                <!-- 注册按钮 -->
                <el-form-item>
                    <el-button class="button" type="primary" auto-insert-space :loading="loading" @click="register">
                        注册
                    </el-button>
                </el-form-item>
                <el-form-item class="flex">
                    <el-link type="info" :underline="false" @click="isRegister = false">
                        ← 返回
                    </el-link>
                </el-form-item>
            </el-form>
            <!-- 登录表单 -->
            <el-form :model="loginData" size="large" autocomplete="off" v-else>
                <el-form-item>
                    <h1>登录</h1>
                </el-form-item>
                <el-form-item>
                    <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="loginData.userName"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-input name="password" :prefix-icon="Lock" v-model="loginData.password" type="password" placeholder="请输入密码" @keyup.enter="login"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-select placeholder="请选择角色" v-model="loginData.role">
                        <el-option v-for="role in roles" :key="role.value" :label="role.label" :value="role.value"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item class="flex">
                    <div class="flex">
                        <el-checkbox>记住我</el-checkbox>
                    </div>
                </el-form-item>
                <!-- 登录按钮 -->
                <el-form-item>
                    <el-button class="button" type="primary" auto-insert-space :loading="loading" @click="login">登录</el-button>
                </el-form-item>
                <el-form-item class="flex">
                    <el-link type="info" :underline="false" @click="isRegister = true">
                        注册 →
                    </el-link>
                    <el-link type="primary" :underline="false" :loading="testing" @click="testConnection">
                        测试后端连接
                    </el-link>
                </el-form-item>
            </el-form>
        </el-col>
    </el-row>
</template>

<style lang="scss" scoped>
/* 样式 */
.login-page {
    height: 100vh;
    background-color: #fff;

    .bg {
        background: url('@/assets/login_bg.jpg') no-repeat center / cover;
        border-radius: 0 20px 20px 0;
    }

    .form {
        display: flex;
        flex-direction: column;
        justify-content: center;
        user-select: none;

        h1 {
            margin: 0;
        }

        .button {
            width: 100%;
        }

        .flex {
            width: 100%;
            display: flex;
            justify-content: space-between;
        }
    }
}
</style>
