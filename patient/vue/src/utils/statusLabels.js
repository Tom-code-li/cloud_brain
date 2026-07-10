const registrationStatusMap = {
  registered: '已挂号',
  waiting_confirmation: '待确认',
  waiting_payment: '待支付',
  unpaid: '待支付',
  paid: '待确认',
  '待确认': '待确认',
  '待支付': '待支付',
  in_visit: '接诊中',
  '接诊中': '接诊中',
  completed: '已完成',
  '已完成': '已完成',
  cancelled: '已取消',
  '已取消': '已取消',
  returned: '已退号',
  '已退号': '已退号',
  no_show: '爽约',
  '爽约': '爽约'
}

const feeStatusMap = {
  unpaid: '待支付',
  paid: '已支付',
  refunded: '已退费',
  '待支付': '待支付',
  '已支付': '已支付',
  '已退费': '已退费'
}

const businessTypeMap = {
  registration: '挂号费',
  exam: '检查检验费',
  lab: '检查检验费',
  prescription: '处方费',
  other: '其他',
  REGISTRATION: '挂号费',
  EXAM_LAB_ORDER: '检查检验费',
  PRESCRIPTION: '处方费',
  OTHER: '其他'
}

const timePeriodMap = {
  morning: '上午',
  afternoon: '下午',
  night: '夜间',
  上午: '上午',
  下午: '下午',
  夜间: '夜间'
}

const scheduleStatusMap = {
  active: '可预约',
  available: '可预约',
  full: '约满',
  stopped: '停诊',
  expired: '已过期',
  '可预约': '可预约',
  '约满': '约满',
  '停诊': '停诊',
  '已过期': '已过期'
}

const itemTypeMap = {
  挂号: '挂号',
  检查: '检查',
  检验: '检验',
  药品: '药品'
}

const reportStatusMap = {
  draft: '草稿',
  published: '已发布',
  reviewed: '已回阅',
  草稿: '草稿',
  已发布: '已发布',
  已回阅: '已回阅'
}

const prescriptionStatusMap = {
  active: '待缴费',
  pending: '待缴费',
  dispensed: '已发药',
  completed: '已完成',
  cancelled: '已取消',
  returned: '已退药',
  待缴费: '待缴费',
  待发药: '待发药',
  发药中: '发药中',
  已发药: '已发药',
  已完成: '已完成',
  已退药: '已退药',
  已取消: '已取消'
}

const prescriptionItemStatusMap = {
  pending: '待发药',
  active: '待发药',
  dispensed: '已发药',
  cancelled: '已取消',
  待发药: '待发药',
  已发药: '已发药',
  已取消: '已取消'
}

const riskLevelMap = {
  normal: '普通',
  urgent: '紧急',
  warning: '紧急',
  high: '紧急',
  critical: '紧急',
  unknown: '普通',
  普通: '普通',
  紧急: '紧急'
}

const sourceMap = {
  online: '线上',
  offline: '线下',
  线上: '线上',
  线下: '线下'
}

export function registrationStatusText(value) {
  return registrationStatusMap[value] || value || ''
}

export function feeStatusText(value) {
  return feeStatusMap[value] || value || ''
}

export function businessTypeText(value) {
  return businessTypeMap[value] || value || ''
}

export function timePeriodText(value) {
  return timePeriodMap[value] || value || ''
}

export function scheduleStatusText(value) {
  return scheduleStatusMap[value] || value || ''
}

export function itemTypeText(value) {
  return itemTypeMap[value] || value || ''
}

export function reportStatusText(value) {
  return reportStatusMap[value] || value || ''
}

export function prescriptionStatusText(value) {
  return prescriptionStatusMap[value] || value || ''
}

export function prescriptionItemStatusText(value) {
  return prescriptionItemStatusMap[value] || value || ''
}

export function riskLevelText(value) {
  return riskLevelMap[value] || value || ''
}

export function sourceText(value) {
  return sourceMap[value] || value || ''
}
