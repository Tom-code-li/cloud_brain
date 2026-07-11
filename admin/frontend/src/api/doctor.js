import request from '@/utils/request'

export function listDoctors(params) {
  return request({ url: '/admin/doctor/list', method: 'get', params })
}

export function checkDoctorDisable(id) {
  return request({ url: '/admin/doctor/' + id + '/disable-check', method: 'get' })
}

export function exportDoctors(departmentId) {
  return request({ url: '/admin/doctor/export', method: 'get', params: { departmentId } })
}

export function createDoctor(data) {
  return request({ url: '/admin/doctor', method: 'post', data })
}

export function updateDoctor(id, data) {
  return request({ url: '/admin/doctor/' + id, method: 'put', data })
}

export function toggleDoctorStatus(id, force) {
  return request({ url: '/admin/doctor/' + id + '/toggle-status', method: 'patch', params: { force: force ? 'true' : 'false' } })
}

export function deleteDoctor(id) {
  return request({ url: '/admin/doctor/' + id, method: 'delete' })
}

export function listDepartments() {
  return request({ url: '/admin/department', method: 'get' })
}

export function listDoctorRoles() {
  return request({ url: '/admin/doctor/roles', method: 'get' })
}