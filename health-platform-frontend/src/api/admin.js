//导入封装好的请求实例
import request from '@/utils/request'

//获取用户列表（分页，params 可选 { role, pageNum, pageSize }）
export const getUsersService = (params) => {
    return request.get('/admin/users', { params })
}

//冻结/解冻用户
export const updateUserStatusService = (userId, data) => {
    return request.put(`/admin/user/${userId}/status`, data)
}

//待审核志愿者列表（分页）
export const getPendingVolunteersService = (params) => {
    return request.get('/admin/volunteers/pending', { params })
}

//审核志愿者（data 建议为 { approved: true/false, reason }，以后端为准）
export const certifyVolunteerService = (userId, data) => {
    return request.put(`/admin/volunteer/${userId}/certify`, data)
}

//查看患者完整档案（含慢性病、服药清单、身份证号等敏感字段）
export const getPatientFullProfileService = (userId) => {
    return request.get(`/admin/patient/${userId}/full-profile`)
}

//服务分类列表
export const getCategoriesService = () => {
    return request.get('/admin/categories')
}

//新增分类
export const addCategoryService = (data) => {
    return request.post('/admin/category', data)
}

//修改分类
export const updateCategoryService = (data) => {
    return request.put('/admin/category', data)
}

//删除分类（报告未写明传参方式，此处按RESTful路径参数预留）
export const deleteCategoryService = (categoryId) => {
    return request.delete(`/admin/category/${categoryId}`)
}

//待处理SOS列表（管理员端5秒轮询，分页）
export const getPendingSosService = (params) => {
    return request.get('/admin/sos/pending', { params })
}

//指派志愿者处理SOS（data 建议为 { volunteerId }，以后端为准）
export const acceptSosService = (sosId, data) => {
    return request.put(`/admin/sos/${sosId}/accept`, data)
}

//完成SOS
export const completeSosService = (sosId) => {
    return request.put(`/admin/sos/${sosId}/complete`)
}

//数据统计看板
export const getDashboardService = () => {
    return request.get('/admin/dashboard')
}

//待派单需求列表（派单管理页，分页）
export const getPendingRequestsService = (params) => {
    return request.get('/admin/requests/pending', { params })
}

//全部任务列表（派单管理页，分页）
export const getTasksService = (params) => {
    return request.get('/admin/tasks', { params })
}

//系统派单：将待接单需求直接派给指定志愿者
export const assignTaskService = (data) => {
    return request.post('/admin/task/assign', data)
}

//系统改派：更换任务志愿者并记录原因
export const reassignTaskService = (taskId, data) => {
    return request.put(`/admin/task/${taskId}/reassign`, data)
}
