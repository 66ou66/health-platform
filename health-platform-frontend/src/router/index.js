//导入vue-router
import { createRouter, createWebHistory } from 'vue-router'
//导入登录页与主布局组件
import LoginVue from '@/views/Login.vue'
import LayoutVue from '@/views/Layout.vue'
//导入状态管理与角色常量（用于路由守卫）
import { useUserStore, ROLE_HOME } from '@/stores/user'
import { ROLE } from '@/utils/constants'

//定义路由关系（各角色页面按需懒加载）
const routes = [
    { path: '/login', component: LoginVue },
    {
        path: '/',
        component: LayoutVue,
        children: [
            // ---------------- 患者端 ----------------
            { path: '', redirect: '/patient/dashboard' },
            { path: 'patient/dashboard', component: () => import('@/views/patient/Dashboard.vue'), meta: { title: '患者首页', role: ROLE.PATIENT } },
            { path: 'patient/publish-request', component: () => import('@/views/patient/PublishRequest.vue'), meta: { title: '发布需求', role: ROLE.PATIENT } },
            { path: 'patient/requests', component: () => import('@/views/patient/RequestList.vue'), meta: { title: '我的需求', role: ROLE.PATIENT } },
            { path: 'patient/health-report', component: () => import('@/views/patient/HealthReport.vue'), meta: { title: '健康上报', role: ROLE.PATIENT } },
            // ---------------- 志愿者端 ----------------
            { path: 'volunteer/task-hall', component: () => import('@/views/volunteer/TaskHall.vue'), meta: { title: '任务大厅', role: ROLE.VOLUNTEER } },
            { path: 'volunteer/my-tasks', component: () => import('@/views/volunteer/MyTasks.vue'), meta: { title: '我的任务', role: ROLE.VOLUNTEER } },
            // ---------------- 管理员端 ----------------
            { path: 'admin/dashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '数据看板', role: ROLE.ADMIN } },
            { path: 'admin/users', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理', role: ROLE.ADMIN } },
            { path: 'admin/verify', component: () => import('@/views/admin/UserVerify.vue'), meta: { title: '志愿者审核', role: ROLE.ADMIN } },
            { path: 'admin/tasks', component: () => import('@/views/admin/TaskAssign.vue'), meta: { title: '派单管理', role: ROLE.ADMIN } },
            { path: 'admin/sos', component: () => import('@/views/admin/SosMonitor.vue'), meta: { title: 'SOS 监控', role: ROLE.ADMIN } },
            { path: 'admin/categories', component: () => import('@/views/admin/CategoryManage.vue'), meta: { title: '分类管理', role: ROLE.ADMIN } },
        ],
    },
    //未匹配的路径回到首页
    { path: '/:pathMatch(.*)*', redirect: '/' },
]

//创建路由器
const router = createRouter({
    history: createWebHistory(),
    routes: routes,
})

//全局前置守卫：登录校验 + 角色权限校验
router.beforeEach((to) => {
    const userStore = useUserStore()
    //当前角色对应的首页（角色值无效时为 undefined，例如旧版本缓存的中文角色）
    const home = ROLE_HOME[userStore.role]

    //未登录 → 跳转登录页
    if (!userStore.isLogin && to.path !== '/login') {
        return { path: '/login', query: { redirect: to.fullPath } }
    }
    //已登录但角色信息失效（缓存是旧版本角色值）→ 清除登录态并回登录页
    if (userStore.isLogin && !home && to.path !== '/login') {
        userStore.logout()
        return { path: '/login' }
    }
    //已登录访问登录页 → 回到自己角色的首页（角色无效则停留在登录页）
    if (userStore.isLogin && to.path === '/login') {
        return home || true
    }
    //访问了其他角色的页面 → 回到自己角色的首页
    if (to.meta.role && to.meta.role !== userStore.role) {
        return home || '/login'
    }

    //设置浏览器标签页标题
    document.title = to.meta.title ? `${to.meta.title} - 健康颐养平台` : '健康颐养平台'
    return true
})

export default router
