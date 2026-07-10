import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '@/App.vue'
import { createAppHistory } from '@/router'

describe('App component', () => {
  it('renders router view entry point', () => {
    const wrapper = mount(App, {
      global: {
        stubs: {
          RouterView: { template: '<main data-test="router-view" />' }
        }
      }
    })

    expect(wrapper.find('[data-test="router-view"]').exists()).toBe(true)
  })
})

describe('router guards', () => {
  let router

  beforeEach(async () => {
    localStorage.clear()
    router = (await import('@/router')).default
  })

  afterEach(() => {
    localStorage.clear()
  })

  it('builds the appropriate history type for test mode', () => {
    expect(createAppHistory('test')).toBeTruthy()
    expect(createAppHistory('production')).toBeTruthy()
  })

  it('redirects protected routes to login when token is missing', async () => {
    await router.push('/dashboard')
    await router.isReady()

    expect(router.currentRoute.value.name).toBe('Login')
    expect(router.currentRoute.value.query.redirect).toBe('/dashboard')
    expect(document.title).toContain('登录')
  })

  it('allows protected routes with a token', async () => {
    localStorage.setItem('admin_token', 'token')

    await router.push('/doctor')
    await router.isReady()

    expect(router.currentRoute.value.name).toBe('Doctor')
  })

  it('loads remaining protected route components', async () => {
    localStorage.setItem('admin_token', 'token')

    await router.push('/department')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('Department')

    await router.push('/role')
    await router.isReady()
    expect(router.currentRoute.value.name).toBe('Role')
  })

  it('loads the schedule route component with a token', async () => {
    localStorage.setItem('admin_token', 'token')

    await router.push('/schedule')
    await router.isReady()

    expect(router.currentRoute.value.name).toBe('Schedule')
  })

  it('redirects logged-in users away from login', async () => {
    localStorage.setItem('admin_token', 'token')

    await router.push('/login')
    await router.isReady()

    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('falls back to unauthenticated behavior when localStorage throws', async () => {
    const getItem = vi.spyOn(localStorage, 'getItem').mockImplementation(() => {
      throw new Error('blocked')
    })

    await router.push('/schedule')
    await router.isReady()

    expect(router.currentRoute.value.name).toBe('Login')
    getItem.mockRestore()
  })

  it('keeps login route accessible without token', async () => {
    await router.push('/login')
    await router.isReady()

    expect(router.currentRoute.value.name).toBe('Login')
  })
})
