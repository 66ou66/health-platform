//导入封装好的请求实例
import request from '@/utils/request'

//注册功能
export const registerService = (data) => {
    return request.post('/auth/register', data)
}

//登录功能
export const loginService = (data) => {
    return request.post('/auth/login', data)
}

//检查用户名是否存在
export const checkUsernameService = (username) => {
    return request.get(`/auth/check/${username}`)
}

//测试前后端连接
export const testConnectionService = () => {
    return request.get('/auth/test')
}
