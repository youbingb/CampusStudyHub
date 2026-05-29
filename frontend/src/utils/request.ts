import axios, { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json; charset=utf-8' }
})

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('csh-token')
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

request.interceptors.response.use(
  (resp: AxiosResponse<ApiResult>) => {
    const r = resp.data
    if (r && typeof r === 'object' && 'code' in r) {
      if (r.code === 0) {
        return r.data as any
      }
      ElMessage.error(r.message || '请求失败')
      return Promise.reject(new Error(r.message || '请求失败'))
    }
    return r as any
  },
  (err: AxiosError<ApiResult>) => {
    const status = err.response?.status
    const msg = err.response?.data?.message || err.message
    if (status === 401) {
      ElMessage.error(msg || '未登录或登录已过期')
      localStorage.removeItem('csh-token')
      if (location.pathname !== '/auth/login') {
        import('@/router').then(({ default: router }) => {
          router.push('/auth/login')
        })
      }
    } else if (status === 403) {
      ElMessage.error('无权限访问')
    } else {
      ElMessage.error(msg || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
