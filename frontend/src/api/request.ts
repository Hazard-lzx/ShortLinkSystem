import axios from 'axios'
import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from './types'
import { storage } from '@/utils/storage'
import router from '@/router'

const request = axios.create({
  baseURL: '',
  timeout: 15000
})

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = storage.getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let redirecting = false
function handleUnauthorized() {
  if (redirecting) return
  redirecting = true
  storage.clearToken()
  storage.clearUsername()
  ElMessage.warning('登录已过期，请重新登录')
  setTimeout(() => {
    redirecting = false
    router.push('/login')
  }, 300)
}

request.interceptors.response.use(
  (response: AxiosResponse<Result>) => {
    const { code, message } = response.data
    if (code === 200) {
      return response
    }
    if (code === 401) {
      handleUnauthorized()
      return Promise.reject(new Error(message))
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    if (error.response?.status === 401) {
      handleUnauthorized()
    } else {
      ElMessage.error(error.response?.data?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export async function get<T>(url: string, params?: object): Promise<T> {
  const res = await request.get<Result<T>>(url, { params })
  return res.data.data
}

export async function post<T>(url: string, data?: object): Promise<T> {
  const res = await request.post<Result<T>>(url, data)
  return res.data.data
}

export async function put<T>(url: string, data?: object): Promise<T> {
  const res = await request.put<Result<T>>(url, data)
  return res.data.data
}

export async function del<T>(url: string): Promise<T> {
  const res = await request.delete<Result<T>>(url)
  return res.data.data
}

export default request
