import { beforeEach, describe, expect, it, vi } from 'vitest'

const messageError = vi.fn()
let requestSuccess
let requestFailure
let responseSuccess
let responseFailure

vi.mock('element-plus', () => ({
  ElMessage: {
    error: messageError
  }
}))

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      interceptors: {
        request: {
          use: vi.fn((success, failure) => {
            requestSuccess = success
            requestFailure = failure
          })
        },
        response: {
          use: vi.fn((success, failure) => {
            responseSuccess = success
            responseFailure = failure
          })
        }
      }
    }))
  }
}))

describe('request utility interceptors', () => {
  beforeEach(async () => {
    vi.resetModules()
    localStorage.clear()
    messageError.mockClear()
    requestSuccess = undefined
    requestFailure = undefined
    responseSuccess = undefined
    responseFailure = undefined
    window.location.hash = ''
    await import('@/utils/request')
  })

  it('adds bearer token when present and leaves headers untouched when absent', () => {
    localStorage.setItem('admin_token', 'abc')

    const authed = requestSuccess({ headers: {} })
    expect(authed.headers.Authorization).toBe('Bearer abc')

    localStorage.removeItem('admin_token')
    const plain = requestSuccess({ headers: {} })
    expect(plain.headers.Authorization).toBeUndefined()
  })

  it('continues when localStorage token read fails and rejects request errors', async () => {
    const getItem = vi.spyOn(localStorage, 'getItem').mockImplementation(() => {
      throw new Error('blocked')
    })

    expect(requestSuccess({ headers: {} }).headers.Authorization).toBeUndefined()
    await expect(requestFailure(new Error('bad config'))).rejects.toThrow('bad config')
    getItem.mockRestore()
  })

  it('unwraps successful business responses and raw responses', () => {
    expect(responseSuccess({ data: { code: 200, data: { ok: true } } })).toEqual({ ok: true })
    expect(responseSuccess({ data: ['raw'] })).toEqual(['raw'])
  })

  it('chooses the correct login redirect path', async () => {
    const { getLoginRedirectPath } = await import('@/utils/request')

    window.location.hash = '#/doctor'
    expect(getLoginRedirectPath()).toBe('/#/login')

    window.location.hash = ''
    expect(getLoginRedirectPath()).toBe('/login')
  })

  it('handles business 401 responses by clearing auth and redirecting', async () => {
    localStorage.setItem('admin_token', 'abc')
    localStorage.setItem('admin_user', '{}')
    window.location.hash = '#/dashboard'

    await expect(responseSuccess({ data: { code: 401, message: 'expired' } })).rejects.toThrow('expired')

    expect(localStorage.getItem('admin_token')).toBeNull()
    expect(localStorage.getItem('admin_user')).toBeNull()
    expect(messageError).toHaveBeenCalledWith('expired')
    expect(window.location.href).toContain('/#/login')
  })

  it('uses hash login redirect when hash routing is present', async () => {
    window.location.hash = '#/doctor'

    await expect(responseSuccess({ data: { code: 401, message: 'expired' } })).rejects.toThrow('expired')

    expect(window.location.href).toContain('/#/login')
  })

  it('redirects to plain login when hash routing is absent', async () => {
    window.location.hash = ''

    await expect(responseSuccess({ data: { code: 401, message: 'expired' } })).rejects.toThrow('expired')

    expect(messageError).toHaveBeenCalledWith('expired')
  })

  it('handles non-401 business failures', async () => {
    await expect(responseSuccess({ data: { code: 500, message: 'failed' } })).rejects.toThrow('failed')

    expect(messageError).toHaveBeenCalledWith('failed')
  })

  it('uses fallback business error text when message is missing', async () => {
    await expect(responseSuccess({ data: { code: 500 } })).rejects.toThrow('请求失败')

    expect(messageError).toHaveBeenCalledWith('请求失败')
  })

  it('handles transport 401 and generic network errors', async () => {
    window.location.hash = ''
    await expect(responseFailure({ response: { status: 401 }, message: 'unauthorized' })).rejects.toMatchObject({
      message: 'unauthorized'
    })
    expect(messageError).toHaveBeenCalled()

    await expect(responseFailure(new Error('network down'))).rejects.toThrow('network down')
    expect(messageError).toHaveBeenCalledWith('network down')
  })

  it('uses fallback network text when error message is missing', async () => {
    await expect(responseFailure({ response: { status: 500 } })).rejects.toMatchObject({
      response: { status: 500 }
    })

    expect(messageError).toHaveBeenCalledWith('网络异常，请稍后重试')
  })

  it('passes through non-object responses and request configs without tokens', () => {
    expect(responseSuccess({ data: 'plain' })).toBe('plain')
    expect(requestSuccess({ headers: {} })).toEqual({ headers: {} })
  })

  it('ignores storage clear failures while handling 401', async () => {
    const removeItem = vi.spyOn(localStorage, 'removeItem').mockImplementation(() => {
      throw new Error('blocked')
    })

    await expect(responseSuccess({ data: { code: 401 } })).rejects.toThrow()

    expect(messageError).toHaveBeenCalled()
    removeItem.mockRestore()
  })
})
