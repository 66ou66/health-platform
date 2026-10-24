//导入封装好的请求实例
import request from '@/utils/request'

//完善/更新我的健康档案
export const updateProfileService = (data) => {
    return request.put('/patient/profile', data)
}

//获取我的健康档案
export const getProfileService = () => {
    return request.get('/patient/profile')
}

//发布服务需求
export const publishRequestService = (data) => {
    return request.post('/patient/request', data)
}

//我的需求列表（分页）
export const getMyRequestsService = (params) => {
    return request.get('/patient/requests', { params })
}

//需求详情
export const getRequestDetailService = (requestId) => {
    return request.get(`/patient/request/${requestId}`)
}

//取消需求
export const cancelRequestService = (requestId) => {
    return request.delete(`/patient/request/${requestId}`)
}

//我的健康记录（分页）
export const getHealthRecordsService = (params) => {
    return request.get('/patient/health-records', { params })
}

//添加健康记录
export const addHealthRecordService = (data) => {
    return request.post('/patient/health-record', data)
}

//SOS一键求助（携带经纬度，后端高德逆地理编码解析地址）
export const sendSosService = (data) => {
    return request.post('/patient/sos', data)
}

//公开的服务分类列表（发布需求时选择）
export const getCategoriesService = () => {
    return request.get('/patient/categories')
}

//评价志愿者
export const submitFeedbackService = (data) => {
    return request.post('/patient/feedback', data)
}

//位置心跳上报（服务中每5秒一次）
export const reportLocationService = (data) => {
    return request.post('/patient/location', data)
}
