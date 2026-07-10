import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const loginApi = vi.fn()
const getAdminInfoApi = vi.fn()

vi.mock('@/api/auth', () => ({
  login: loginApi,
  getAdminInfo: getAdminInfoApi
}))

describe('user store', () => {
  beforeEach(() => {
    localStorage.clear()
    loginApi.mockReset()
    getAdminInfoApi.mockReset()
    vi.resetModules()
    setActivePinia(createPinia())
  })

  it('loads token and user from localStorage', async () => {
    localStorage.setItem('admin_token', 'token-1')
    localStorage.setItem('admin_user', JSON.stringify({ username: 'admin', realName: 'Admin', roleCode: 'ADMIN', roleName: 'Manager' }))
    const { useUserStore } = await import('@/stores/user')

    const store = useUserStore()

    expect(store.isLoggedIn).toBe(true)
    expect(store.username).toBe('admin')
    expect(store.realName).toBe('Admin')
    expect(store.roleCode).toBe('ADMIN')
    expect(store.roleName).toBe('Manager')
  })

  it('handles invalid stored user and storage failures', async () => {
    localStorage.setItem('admin_user', '{bad json')
    const getItem = vi.spyOn(localStorage, 'getItem').mockImplementation(() => {
      throw new Error('blocked')
    })
    const { useUserStore } = await import('@/stores/user')

    const store = useUserStore()

    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
    getItem.mockRestore()
  })

  it('exposes default getters when no user is stored', async () => {
    const { useUserStore } = await import('@/stores/user')

    const store = useUserStore()

    expect(store.isLoggedIn).toBe(false)
    expect(store.username).toBe('')
    expect(store.realName).toBe('')
    expect(store.roleCode).toBe('')
    expect(store.roleName).toBe('')
  })

  it('logs in and persists normalized user info', async () => {
    const { useUserStore } = await import('@/stores/user')
    loginApi.mockResolvedValue({
      token: 'token-2',
      userId: 1,
      username: 'root',
      realName: 'Root',
      roleCode: 'ADMIN',
      roleName: 'Administrator'
    })

    const store = useUserStore()
    const user = await store.login({ username: 'root', password: 'pw' })

    expect(loginApi).toHaveBeenCalledWith({ username: 'root', password: 'pw' })
    expect(store.token).toBe('token-2')
    expect(user).toEqual({
      userId: 1,
      username: 'root',
      realName: 'Root',
      roleCode: 'ADMIN',
      roleName: 'Administrator'
    })
    expect(localStorage.getItem('admin_token')).toBe('token-2')
  })

  it('fetches user info and tolerates localStorage write failures', async () => {
    const { useUserStore } = await import('@/stores/user')
    getAdminInfoApi.mockResolvedValue({
      userId: 2,
      username: 'doctor',
      realName: 'Doctor',
      roleCode: 'DOCTOR',
      roleName: 'Doctor'
    })
    const setItem = vi.spyOn(localStorage, 'setItem').mockImplementation(() => {
      throw new Error('quota')
    })

    const store = useUserStore()
    const user = await store.fetchInfo()

    expect(user.username).toBe('doctor')
    expect(store.roleName).toBe('Doctor')
    setItem.mockRestore()
  })

  it('logs out and tolerates storage removal failures', async () => {
    const { useUserStore } = await import('@/stores/user')
    const removeItem = vi.spyOn(localStorage, 'removeItem').mockImplementation(() => {
      throw new Error('blocked')
    })

    const store = useUserStore()
    store.token = 'token'
    store.userInfo = { username: 'admin' }
    store.logout()

    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
    removeItem.mockRestore()
  })

  it('reads empty storage without throwing', async () => {
    localStorage.clear()
    const { useUserStore } = await import('@/stores/user')

    const store = useUserStore()

    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
  })
})
