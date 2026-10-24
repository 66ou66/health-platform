<script setup>
//导入图标
import {
    HomeFilled,
    EditPen,
    Tickets,
    TrendCharts,
    Grid,
    List,
    DataAnalysis,
    User,
    Stamp,
    Bell,
    CollectionTag,
    Position,
    SwitchButton,
    CaretBottom,
} from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
//头像：患者/志愿者用专属头像，管理员用默认头像
import avatarDefault from '@/assets/default.png'
import avatarPatient from '@/assets/huanzhe.png'
import avatarVolunteer from '@/assets/zhiyuanzhe.png'
import { useUserStore } from '@/stores/user'
import { ROLE, ROLE_TEXT, ROLE_TAG_TYPE } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

//各角色侧边栏菜单
const menuMap = {
    [ROLE.PATIENT]: [
        { path: '/patient/dashboard', title: '患者首页', icon: HomeFilled },
        { path: '/patient/publish-request', title: '发布需求', icon: EditPen },
        { path: '/patient/requests', title: '我的需求', icon: Tickets },
        { path: '/patient/health-report', title: '健康上报', icon: TrendCharts },
    ],
    [ROLE.VOLUNTEER]: [
        { path: '/volunteer/task-hall', title: '任务大厅', icon: Grid },
        { path: '/volunteer/my-tasks', title: '我的任务', icon: List },
    ],
    [ROLE.ADMIN]: [
        { path: '/admin/dashboard', title: '数据看板', icon: DataAnalysis },
        { path: '/admin/users', title: '用户管理', icon: User },
        { path: '/admin/verify', title: '志愿者审核', icon: Stamp },
        { path: '/admin/tasks', title: '派单管理', icon: Position },
        { path: '/admin/sos', title: 'SOS 监控', icon: Bell },
        { path: '/admin/categories', title: '分类管理', icon: CollectionTag },
    ],
}

const menus = computed(() => menuMap[userStore.role] || [])
const roleText = computed(() => ROLE_TEXT[userStore.role] || userStore.role)
const roleTagType = computed(() => ROLE_TAG_TYPE[userStore.role] || 'info')

//右上角头像按角色切换
const avatarByRole = {
    [ROLE.PATIENT]: avatarPatient,
    [ROLE.VOLUNTEER]: avatarVolunteer,
}
const avatar = computed(() => avatarByRole[userStore.role] || avatarDefault)

//退出登录
const logout = async () => {
    try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '退出',
            cancelButtonText: '取消',
            type: 'warning',
        })
    } catch {
        return //用户取消
    }
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
}

//下拉菜单点击事件
const handleCommand = (command) => {
    if (command === 'logout') logout()
}
</script>

<template>
    <el-container class="layout-container">
        <!-- 左侧菜单 -->
        <el-aside width="200px">
            <div class="el-aside__logo">健康颐养平台</div>
            <el-menu active-text-color="#ffd04b" background-color="#309bcc" text-color="#fff" router :default-active="route.path">
                <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
                    <el-icon>
                        <component :is="item.icon" />
                    </el-icon>
                    <span>{{ item.title }}</span>
                </el-menu-item>
            </el-menu>
        </el-aside>
        <!-- 右侧主区域 -->
        <el-container>
            <!-- 头部区域 -->
            <el-header>
                <div>健康颐养平台：<strong>{{ userStore.displayName || '未登录' }}</strong></div>
                <el-dropdown placement="bottom-end" @command="handleCommand">
                    <span class="el-dropdown__box">
                        <el-avatar :src="avatar" />
                        <span>{{ roleText }}</span>
                        <el-icon>
                            <CaretBottom />
                        </el-icon>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </el-header>
            <!-- 中间区域 -->
            <el-main>
                <RouterView />
            </el-main>
            <!-- 底部区域 -->
            <el-footer>健康颐养平台 ©2026 Created by 大连民大</el-footer>
        </el-container>
    </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
    height: 100vh;

    .el-aside {
        background-color: #309bcc;

        &__logo {
            height: 120px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            font-weight: 600;
            color: #fff;
            letter-spacing: 2px;
        }

        .el-menu {
            border-right: none;
        }
    }

    .el-header {
        background-color: #fff;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .el-dropdown__box {
            display: flex;
            align-items: center;
            cursor: pointer;

            .el-avatar {
                margin-right: 6px;
            }

            span {
                margin-right: 4px;
            }

            .el-icon {
                color: #999;
                margin-left: 6px;
            }

            &:active,
            &:focus {
                outline: none;
            }
        }
    }

    .el-main {
        padding: 20px;
        overflow-y: auto;
    }

    .el-footer {
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        color: #666;
        background-color: #fff;
    }
}
</style>
