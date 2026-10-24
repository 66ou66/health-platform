import { defineStore } from 'pinia'
import { ROLE } from '@/utils/constants'

/**
 * 用户状态管理（Pinia）
 * 持久化：token 与用户信息存在 localStorage，刷新页面不丢失登录态
 */

/** 安全读取缓存的用户信息（localStorage 被篡改/损坏时不至于崩溃） */
function readUserInfo() {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || 'null')
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: readUserInfo(),
  }),

  getters: {
    isLogin: (state) => !!state.token,
    /** 当前角色：0管理员 1患者 2志愿者（注意 0 是合法角色，不能用 || 判断） */
    role: (state) => (state.userInfo?.role != null ? state.userInfo.role : ''),
    /** 显示名称：优先真实姓名，其次用户名 */
    displayName: (state) => state.userInfo?.realName || state.userInfo?.username || '',
  },

  actions: {
    /** 登录成功后保存登录态（userInfo 字段以后端 LoginRespVo 为准） */
    setLogin(token, userInfo) {
      this.token = token
      this.userInfo = userInfo
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    /** 退出登录，清除本地登录态 */
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
  },
})

/** 各角色登录后的默认首页 */
export const ROLE_HOME = {
  [ROLE.PATIENT]: '/patient/dashboard',
  [ROLE.VOLUNTEER]: '/volunteer/task-hall',
  [ROLE.ADMIN]: '/admin/dashboard',
}
