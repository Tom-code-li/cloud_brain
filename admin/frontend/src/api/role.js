import request from '@/utils/request'

export function listRoles() {
  return request({ url: '/admin/roles', method: 'get' })
}

export function getRoleDetail(id) {
  return request({ url: `/admin/roles/${id}`, method: 'get' })
}

export function createRole(data) {
  return request({ url: '/admin/roles', method: 'post', data })
}

export function updateRole(id, data) {
  return request({ url: `/admin/roles/${id}`, method: 'put', data })
}

export function deleteRole(id) {
  return request({ url: `/admin/roles/${id}`, method: 'delete' })
}

export function toggleRoleStatus(id) {
  return request({ url: `/admin/roles/${id}/toggle-status`, method: 'patch' })
}