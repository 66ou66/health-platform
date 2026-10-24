//定制请求的实例
//导入axios  npm install axios
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

//定义一个变量,记录公共的前缀  ,  baseURL
//开发环境连本地后端（后端已按课程示例加 @CrossOrigin，可直接跨域）；
//部署到服务器后把 localhost:8080 改成服务器地址，例如 http://121.40.211.186:8080/health_platform/
const baseURL = 'http://localhost:8080/health_platform/'
const instance = axios.create({ baseURL })

//添加请求拦截器：统一携带登录令牌 token
instance.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.token = userStore.token
  }
  return config
})

//添加响应拦截器
instance.interceptors.response.use(
  (result) => {
    //后端统一返回结构 Result：{ code, message, data }
    //code 为 0 表示成功（与课程示例一致），兼容 200 的写法
    const res = result.data
    //兜底：后端若未按 Result 包装（如连接测试直接返回字符串），原样返回
    if (res === null || res === undefined || typeof res !== 'object' || !('code' in res)) {
      return res
    }
    if (res.code === 0 || res.code === 200) {
      return res.data
    }
    //登录过期：清除登录状态并回到登录页
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error('未登录或登录已过期'))
    }
    //失败提示
    ElMessage.error(res.message || res.msg || '请求失败')
    return Promise.reject(new Error(res.message || res.msg || '请求失败'))
  },
  (err) => {
    //HTTP 401：同样视为登录过期
    if (err.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error('服务异常')
    }
    return Promise.reject(err) //异步的状态转化成失败的状态
  },
)

export default instance
