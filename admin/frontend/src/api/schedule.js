import request from '@/utils/request'

// 部门列表
export function listDepartments() {
  return request({
    url: '/admin/department',
    method: 'get'
  })
}

// 某部门下的医生列表
export function listDoctors(departmentId) {
  return request({
    url: '/admin/doctors',
    method: 'get',
    params: { departmentId }
  })
}

// 排班列表
export function listSchedules(params) {
  return request({
    url: '/admin/schedules',
    method: 'get',
    params
  })
}

// 排班详情
export function getScheduleDetail(id) {
  return request({
    url: '/admin/schedules/' + id,
    method: 'get'
  })
}

// 某排班下的挂号记录
export function listScheduleRegistrations(id) {
  return request({
    url: '/admin/schedules/' + id + '/registrations',
    method: 'get'
  })
}

// 创建排班
export function createSchedule(data) {
  return request({
    url: '/admin/schedules',
    method: 'post',
    data
  })
}

// 批量创建排班
export function batchCreateSchedule(data) {
  return request({
    url: '/admin/schedules/batch',
    method: 'post',
    data
  })
}

// 更新排班
export function updateSchedule(id, data) {
  return request({
    url: '/admin/schedules/' + id,
    method: 'put',
    data
  })
}

// 取消排班
export function cancelSchedule(id) {
  return request({
    url: '/admin/schedules/' + id,
    method: 'delete'
  })
}

// AI 建议
export function generateScheduleSuggestion(data) {
  return request({
    url: '/admin/schedules/ai-suggestions',
    method: 'post',
    data
  })
}

export function acceptSuggestion(suggestionId) {
  return request({
    url: '/admin/schedules/ai-suggestions/' + suggestionId + '/accept',
    method: 'post'
  })
}

export function rejectSuggestion(suggestionId) {
  return request({
    url: '/admin/schedules/ai-suggestions/' + suggestionId + '/reject',
    method: 'post'
  })
}

export function acceptSuggestionDetail(suggestionId, detailId) {
  return request({
    url:
      '/admin/schedules/ai-suggestions/' +
      suggestionId +
      '/details/' +
      detailId +
      '/accept',
    method: 'post'
  })
}

export function rejectSuggestionDetail(suggestionId, detailId) {
  return request({
    url:
      '/admin/schedules/ai-suggestions/' + suggestionId + '/details/' + detailId + '/reject',
    method: 'post'
  })
}