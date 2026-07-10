import axios from 'axios'
import { ElMessage } from 'element-plus'

const TOKEN_KEY = 'admin_token'
const USER_KEY = 'admin_user'

function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY) || ''
  } catch {
    return ''
  }
}

function clearAuth() {
  try {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  } catch {
    // ignore
  }
}

export function getLoginRedirectPath() {
  return window.location.hash ? '/#/login' : '/login'
}

const request = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result && typeof result === 'object' && 'code' in result) {
      if (result.code === 200) {
        return result.data
      }
      if (result.code === 401) {
        clearAuth()
        ElMessage.error(result.message || '登录已过期，请重新登录')
        window.location.href = getLoginRedirectPath()
        return Promise.reject(new Error(result.message || '未授权'))
      }
      ElMessage.error(result.message || '请求失败')
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    return result
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      clearAuth()
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = getLoginRedirectPath()
    } else {
      ElMessage.error(error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
