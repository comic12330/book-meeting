/**
 * axios 实例与两个拦截器。
 *
 * 后端约定 HTTP 状态码恒为 200，真正的结果放在响应体的 code 字段里，
 * 所以这里必须看 res.code 而不是 HTTP 状态码，否则 401 之类的业务失败会被当成成功。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '../utils/auth'

const request = axios.create({
  // vite 代理到后端 8080，见 vite.config.js
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：自动带上令牌，业务代码里不用每次手动拼请求头
request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    // 401 说明令牌失效或被停用：清掉本地登录态并回登录页。
    // 判断一次当前路径，避免已经在登录页时反复跳转。
    if (res.code === 401) {
      clearAuth()
      ElMessage.error(res.msg || '登录已失效')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.msg || '登录已失效'))
    }
    // 其余业务失败统一提示后端返回的文案，例如「该会议室在 X 至 Y 之间已被预定」
    ElMessage.error(res.msg || '操作失败')
    return Promise.reject(new Error(res.msg || '操作失败'))
  },
  (error) => {
    // 走到这里通常是后端没启动、网络断开或超时，优先取后端返回的 msg
    const msg = error.response && error.response.data && error.response.data.msg
      ? error.response.data.msg
      : '网络异常，请检查后端服务是否已启动'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
