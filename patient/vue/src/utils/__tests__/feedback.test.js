import { describe, it, expect, beforeEach } from 'vitest'
import feedback, { setFeedbackHandlers } from '../feedback'

describe('feedback', () => {
  describe('toast', () => {
    it('should call the toast handler with message and type', () => {
      const calls = []
      setFeedbackHandlers({
        toast: (message, type) => calls.push({ message, type }),
      })

      feedback.toast('操作成功', 'success')

      expect(calls).toHaveLength(1)
      expect(calls[0].message).toBe('操作成功')
      expect(calls[0].type).toBe('success')
    })

    it('should use default type "info" when not provided', () => {
      const calls = []
      setFeedbackHandlers({
        toast: (message, type) => calls.push({ message, type }),
      })

      feedback.toast('提示信息')

      expect(calls[0].type).toBe('info')
    })

    it('should handle error type toast', () => {
      const calls = []
      setFeedbackHandlers({
        toast: (message, type) => calls.push({ message, type }),
      })

      feedback.toast('出错了', 'error')

      expect(calls[0].type).toBe('error')
    })
  })

  describe('confirm', () => {
    it('should call the confirm handler and return true', async () => {
      setFeedbackHandlers({
        confirm: async () => true,
      })

      const result = await feedback.confirm('确定要删除吗？')

      expect(result).toBe(true)
    })

    it('should call the confirm handler and return false', async () => {
      setFeedbackHandlers({
        confirm: async () => false,
      })

      const result = await feedback.confirm('确定要取消吗？')

      expect(result).toBe(false)
    })

    it('should return false by default when no handler is set', async () => {
      // reset handlers to defaults
      setFeedbackHandlers({})

      const result = await feedback.confirm('测试')

      expect(result).toBe(false)
    })
  })

  describe('setFeedbackHandlers', () => {
    it('should partially update handlers', () => {
      const toastCalls = []
      setFeedbackHandlers({
        toast: (msg) => toastCalls.push(msg),
      })

      feedback.toast('仅更新toast')
      feedback.confirm('测试confirm') // should not throw

      expect(toastCalls).toHaveLength(1)
    })

    it('should not break when called with null', () => {
      expect(() => setFeedbackHandlers(null)).not.toThrow()
    })
  })
})
