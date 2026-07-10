import { vi } from 'vitest'

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn()
  }))
})

const storageState = new Map()
const localStorageMock = {
  getItem(key) {
    return storageState.has(key) ? storageState.get(key) : null
  },
  setItem(key, value) {
    storageState.set(String(key), String(value))
  },
  removeItem(key) {
    storageState.delete(String(key))
  },
  clear() {
    storageState.clear()
  },
  key(index) {
    return Array.from(storageState.keys())[index] || null
  },
  get length() {
    return storageState.size
  }
}

Object.defineProperty(window, 'localStorage', {
  writable: true,
  value: localStorageMock
})

Object.defineProperty(globalThis, 'localStorage', {
  writable: true,
  value: localStorageMock
})
