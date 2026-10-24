//导入封装好的请求实例
import request from '@/utils/request'

//完善/更新志愿者档案（含实名认证信息、技能标签等）
export const updateProfileService = (data) => {
    return request.put('/volunteer/profile', data)
}

//获取我的志愿者档案
export const getProfileService = () => {
    return request.get('/volunteer/profile')
}

//任务大厅（待接单需求列表，分页）
export const getPendingTasksService = (params) => {
    return request.get('/volunteer/tasks/pending', { params })
}

//抢单
export const claimTaskService = (requestId) => {
    return request.post(`/volunteer/task/${requestId}/claim`)
}

//我的任务列表（分页）
export const getMyTasksService = (params) => {
    return request.get('/volunteer/tasks/my', { params })
}

//任务签到（body 对应 SignInReqVo，预计为经纬度/地址字段）
export const signInService = (taskId, data) => {
    return request.post(`/volunteer/task/${taskId}/sign-in`, data)
}

//任务签退
export const signOutService = (taskId, data) => {
    return request.post(`/volunteer/task/${taskId}/sign-out`, data)
}

//任务详情（含患者脱敏信息，对应 TaskDetailRespVo）
export const getTaskDetailService = (taskId) => {
    return request.get(`/volunteer/task/${taskId}`)
}

//评价患者
export const submitFeedbackService = (data) => {
    return request.post('/volunteer/feedback', data)
}

//查询服务中任务的患者实时位置
export const getPatientLocationService = (taskId) => {
    return request.get(`/volunteer/task/${taskId}/patient-location`)
}
