/**
 * 全局常量与枚举映射（与数据库枚举约定一致）
 */

/** 角色（与 users.role 一致） */
export const ROLE = {
  ADMIN: 0,
  PATIENT: 1,
  VOLUNTEER: 2,
}

export const ROLE_TEXT = {
  [ROLE.ADMIN]: '管理员',
  [ROLE.PATIENT]: '患者',
  [ROLE.VOLUNTEER]: '志愿者',
}

export const ROLE_TAG_TYPE = {
  [ROLE.ADMIN]: 'danger',
  [ROLE.PATIENT]: 'success',
  [ROLE.VOLUNTEER]: 'warning',
}

/** 服务需求状态（service_requests.status） */
export const REQUEST_STATUS = {
  0: { text: '待接单', type: 'warning' },
  1: { text: '已接单', type: 'primary' },
  2: { text: '服务中', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' },
}

/** 任务状态（task_assignments.status） */
export const TASK_STATUS = {
  0: { text: '已接单·待签到', type: 'primary' },
  1: { text: '服务中', type: 'warning' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已取消', type: 'info' },
}

/** 指派方式（task_assignments.assign_type） */
export const ASSIGN_TYPE = {
  1: { text: '志愿者抢单', type: 'info' },
  2: { text: '管理员指派', type: 'warning' },
}

/** SOS 求助状态（sos_alerts.status） */
export const SOS_STATUS = {
  0: { text: '待处理', type: 'danger' },
  1: { text: '已指派', type: 'warning' },
  2: { text: '已完成', type: 'success' },
}

/** 用户账号状态（users.status：1正常 0冻结） */
export const USER_STATUS = {
  1: { text: '正常', type: 'success' },
  0: { text: '已冻结', type: 'danger' },
}

/** 志愿者认证状态（volunteer_profiles.cert_status） */
export const CERT_STATUS = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已驳回', type: 'danger' },
}

/** 心情状态选项（health_records.mood_status） */
export const MOOD_OPTIONS = ['开心', '平静', '焦虑', '低落']

/**
 * 服务分类兜底列表（后端分类接口失败时使用）
 */
export const DEFAULT_CATEGORIES = [
  { id: 1, categoryName: '陪诊就医' },
  { id: 2, categoryName: '买药送药' },
  { id: 3, categoryName: '生活照料' },
  { id: 4, categoryName: '心理陪伴' },
  { id: 5, categoryName: '家政服务' },
]
