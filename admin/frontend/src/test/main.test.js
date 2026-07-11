import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const appUse = vi.fn().mockReturnThis()
const appMount = vi.fn()
const appInstance = {
  use: appUse,
  mount: appMount
}
const createApp = vi.fn(() => appInstance)
const createPinia = vi.fn(() => ({ pinia: true }))
const routerStub = { router: true }
const elementPlusStub = { elementPlus: true }

vi.mock('vue', () => ({
  createApp
}))

vi.mock('pinia', () => ({
  createPinia
}))

vi.mock('@/router', () => ({
  default: routerStub
}))

vi.mock('element-plus', () => ({
  default: elementPlusStub
}))

describe('application bootstrap', () => {
  beforeEach(() => {
    createApp.mockClear()
    createPinia.mockClear()
    appUse.mockClear()
    appMount.mockClear()
  })

  afterEach(() => {
    vi.resetModules()
  })

  it('wires app plugins and mounts the root app', async () => {
    await import('@/main')

    expect(createApp).toHaveBeenCalledTimes(1)
    expect(createPinia).toHaveBeenCalledTimes(1)
    expect(appUse).toHaveBeenNthCalledWith(1, { pinia: true })
    expect(appUse).toHaveBeenNthCalledWith(2, routerStub)
    expect(appUse).toHaveBeenNthCalledWith(3, elementPlusStub, { locale: expect.any(Object) })
    expect(appMount).toHaveBeenCalledWith('#app')
  })
})
