import { defineStore } from 'pinia'
import { login, getAdminInfo } from '@/api/auth'

const TOKEN_KEY = 'admin_token'
const USER_KEY = 'admin_user'

function readToken() {
  try {
    return localStorage.getItem(TOKEN_KEY) || ''
  } catch {
    return ''
  }
}

function readUser() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: readToken(),
    userInfo: readUser()
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    realName: (state) => state.userInfo?.realName || '',
    roleCode: (state) => state.userInfo?.roleCode || '',
    roleName: (state) => state.userInfo?.roleName || ''
  },
  actions: {
    async login({ username, password }) {
      const data = await login({ username, password })
      this.token = data.token || ''
      this.userInfo = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        roleCode: data.roleCode,
        roleName: data.roleName
      }
      try {
        localStorage.setItem(TOKEN_KEY, this.token)
        localStorage.setItem(USER_KEY, JSON.stringify(this.userInfo))
      } catch {
        // ignore storage error
      }
      return this.userInfo
    },
    async fetchInfo() {
      const data = await getAdminInfo()
      this.userInfo = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        roleCode: data.roleCode,
        roleName: data.roleName
      }
      try {
        localStorage.setItem(USER_KEY, JSON.stringify(this.userInfo))
      } catch {
        // ignore
      }
      return this.userInfo
    },
    logout() {
      this.token = ''
      this.userInfo = null
      try {
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
      } catch {
        // ignore
      }
    }
  }
})