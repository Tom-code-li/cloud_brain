import request from '@/utils/request'

const BASE = '/admin/department'

export function listDepartments(keyword, status) {
  return request({
    url: BASE,
    method: 'get',
    params: { keyword, status }
  })
}

export function getDepartmentTree() {
  return request({
    url: BASE + '/tree',
    method: 'get'
  })
}

export function getDepartmentDetail(id) {
  return request({
    url: BASE + '/' + id,
    method: 'get'
  })
}

export function createDepartment(data) {
  return request({
    url: BASE,
    method: 'post',
    data
  })
}

export function updateDepartment(id, data) {
  return request({
    url: BASE + '/' + id,
    method: 'put',
    data
  })
}

export function toggleDepartmentStatus(id) {
  return request({
    url: BASE + '/' + id + '/toggle-status',
    method: 'post'
  })
}

export function deleteDepartment(id) {
  return request({
    url: BASE + '/' + id,
    method: 'delete'
  })
}