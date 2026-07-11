import { describe, it, expect } from 'vitest'
import {
  registrationStatusText,
  feeStatusText,
  businessTypeText,
  timePeriodText,
  scheduleStatusText,
  itemTypeText,
  reportStatusText,
  prescriptionStatusText,
  prescriptionItemStatusText,
  riskLevelText,
  sourceText,
} from '../statusLabels'

describe('statusLabels', () => {
  describe('registrationStatusText', () => {
    it('should return correct text for English status keys', () => {
      expect(registrationStatusText('registered')).toBe('已挂号')
      expect(registrationStatusText('waiting_confirmation')).toBe('待确认')
      expect(registrationStatusText('waiting_payment')).toBe('待支付')
      expect(registrationStatusText('unpaid')).toBe('待支付')
      expect(registrationStatusText('in_visit')).toBe('接诊中')
      expect(registrationStatusText('completed')).toBe('已完成')
      expect(registrationStatusText('cancelled')).toBe('已取消')
      expect(registrationStatusText('returned')).toBe('已退号')
      expect(registrationStatusText('no_show')).toBe('爽约')
    })

    it('should return same text for Chinese status keys', () => {
      expect(registrationStatusText('待确认')).toBe('待确认')
      expect(registrationStatusText('接诊中')).toBe('接诊中')
      expect(registrationStatusText('已完成')).toBe('已完成')
      expect(registrationStatusText('已取消')).toBe('已取消')
      expect(registrationStatusText('爽约')).toBe('爽约')
    })

    it('should return the original value for unknown keys', () => {
      expect(registrationStatusText('未知状态')).toBe('未知状态')
    })

    it('should return empty string for null or undefined', () => {
      expect(registrationStatusText(null)).toBe('')
      expect(registrationStatusText(undefined)).toBe('')
      expect(registrationStatusText('')).toBe('')
    })
  })

  describe('feeStatusText', () => {
    it('should return correct text for English keys', () => {
      expect(feeStatusText('unpaid')).toBe('待支付')
      expect(feeStatusText('paid')).toBe('已支付')
      expect(feeStatusText('refunded')).toBe('已退费')
    })

    it('should return correct text for Chinese keys', () => {
      expect(feeStatusText('待支付')).toBe('待支付')
      expect(feeStatusText('已支付')).toBe('已支付')
      expect(feeStatusText('已退费')).toBe('已退费')
    })

    it('should return original for unknown', () => {
      expect(feeStatusText('unknown')).toBe('unknown')
    })

    it('should return empty for null', () => {
      expect(feeStatusText(null)).toBe('')
    })
  })

  describe('businessTypeText', () => {
    it('should return correct text for English keys', () => {
      expect(businessTypeText('registration')).toBe('挂号费')
      expect(businessTypeText('exam')).toBe('检查检验费')
      expect(businessTypeText('lab')).toBe('检查检验费')
      expect(businessTypeText('prescription')).toBe('处方费')
      expect(businessTypeText('other')).toBe('其他')
    })

    it('should return correct text for uppercase keys', () => {
      expect(businessTypeText('REGISTRATION')).toBe('挂号费')
      expect(businessTypeText('EXAM_LAB_ORDER')).toBe('检查检验费')
      expect(businessTypeText('PRESCRIPTION')).toBe('处方费')
      expect(businessTypeText('OTHER')).toBe('其他')
    })

    it('should return original for unknown', () => {
      expect(businessTypeText('unknown')).toBe('unknown')
    })
  })

  describe('timePeriodText', () => {
    it('should return correct text', () => {
      expect(timePeriodText('morning')).toBe('上午')
      expect(timePeriodText('afternoon')).toBe('下午')
      expect(timePeriodText('night')).toBe('夜间')
    })

    it('should handle Chinese keys', () => {
      expect(timePeriodText('上午')).toBe('上午')
      expect(timePeriodText('下午')).toBe('下午')
      expect(timePeriodText('夜间')).toBe('夜间')
    })
  })

  describe('scheduleStatusText', () => {
    it('should return correct text for English keys', () => {
      expect(scheduleStatusText('active')).toBe('可预约')
      expect(scheduleStatusText('available')).toBe('可预约')
      expect(scheduleStatusText('full')).toBe('约满')
      expect(scheduleStatusText('stopped')).toBe('停诊')
      expect(scheduleStatusText('expired')).toBe('已过期')
    })

    it('should handle Chinese keys', () => {
      expect(scheduleStatusText('可预约')).toBe('可预约')
      expect(scheduleStatusText('约满')).toBe('约满')
      expect(scheduleStatusText('停诊')).toBe('停诊')
    })
  })

  describe('itemTypeText', () => {
    it('should return correct text', () => {
      expect(itemTypeText('挂号')).toBe('挂号')
      expect(itemTypeText('检查')).toBe('检查')
      expect(itemTypeText('检验')).toBe('检验')
      expect(itemTypeText('药品')).toBe('药品')
    })

    it('should return original for unknown', () => {
      expect(itemTypeText('未知')).toBe('未知')
    })
  })

  describe('reportStatusText', () => {
    it('should return correct text for English keys', () => {
      expect(reportStatusText('draft')).toBe('草稿')
      expect(reportStatusText('published')).toBe('已发布')
      expect(reportStatusText('reviewed')).toBe('已回阅')
    })

    it('should handle Chinese keys', () => {
      expect(reportStatusText('草稿')).toBe('草稿')
      expect(reportStatusText('已发布')).toBe('已发布')
      expect(reportStatusText('已回阅')).toBe('已回阅')
    })
  })

  describe('prescriptionStatusText', () => {
    it('should return correct text for English keys', () => {
      expect(prescriptionStatusText('active')).toBe('待缴费')
      expect(prescriptionStatusText('pending')).toBe('待缴费')
      expect(prescriptionStatusText('dispensed')).toBe('已发药')
      expect(prescriptionStatusText('completed')).toBe('已完成')
      expect(prescriptionStatusText('cancelled')).toBe('已取消')
      expect(prescriptionStatusText('returned')).toBe('已退药')
    })

    it('should handle Chinese keys', () => {
      expect(prescriptionStatusText('待缴费')).toBe('待缴费')
      expect(prescriptionStatusText('已发药')).toBe('已发药')
      expect(prescriptionStatusText('已取消')).toBe('已取消')
    })
  })

  describe('prescriptionItemStatusText', () => {
    it('should return correct text for English keys', () => {
      expect(prescriptionItemStatusText('pending')).toBe('待发药')
      expect(prescriptionItemStatusText('active')).toBe('待发药')
      expect(prescriptionItemStatusText('dispensed')).toBe('已发药')
      expect(prescriptionItemStatusText('cancelled')).toBe('已取消')
    })

    it('should handle Chinese keys', () => {
      expect(prescriptionItemStatusText('待发药')).toBe('待发药')
      expect(prescriptionItemStatusText('已发药')).toBe('已发药')
    })
  })

  describe('riskLevelText', () => {
    it('should return correct text for English keys', () => {
      expect(riskLevelText('normal')).toBe('普通')
      expect(riskLevelText('urgent')).toBe('紧急')
      expect(riskLevelText('warning')).toBe('紧急')
      expect(riskLevelText('high')).toBe('紧急')
      expect(riskLevelText('critical')).toBe('紧急')
      expect(riskLevelText('unknown')).toBe('普通')
    })

    it('should handle Chinese keys', () => {
      expect(riskLevelText('普通')).toBe('普通')
      expect(riskLevelText('紧急')).toBe('紧急')
    })
  })

  describe('sourceText', () => {
    it('should return correct text for English keys', () => {
      expect(sourceText('online')).toBe('线上')
      expect(sourceText('offline')).toBe('线下')
    })

    it('should handle Chinese keys', () => {
      expect(sourceText('线上')).toBe('线上')
      expect(sourceText('线下')).toBe('线下')
    })

    it('should return original for unknown', () => {
      expect(sourceText('unknown')).toBe('unknown')
    })

    it('should return empty for null', () => {
      expect(sourceText(null)).toBe('')
    })
  })
})
