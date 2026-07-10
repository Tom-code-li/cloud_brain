import { beforeEach, describe, expect, it, vi } from 'vitest'

const request = vi.fn()

vi.mock('@/utils/request', () => ({
  default: request
}))

describe('api modules', () => {
  beforeEach(() => {
    request.mockClear()
  })

  it('builds auth requests', async () => {
    const { login, getAdminInfo } = await import('@/api/auth')
    const payload = { username: 'admin', password: 'secret' }

    login(payload)
    getAdminInfo()

    expect(request).toHaveBeenNthCalledWith(1, { url: '/auth/login', method: 'post', data: payload })
    expect(request).toHaveBeenNthCalledWith(2, { url: '/auth/info', method: 'get' })
  })

  it('builds department requests', async () => {
    const api = await import('@/api/department')
    const data = { name: 'Cardiology' }

    api.listDepartments('card', 1)
    api.getDepartmentTree()
    api.getDepartmentDetail(3)
    api.createDepartment(data)
    api.updateDepartment(3, data)
    api.toggleDepartmentStatus(3)
    api.deleteDepartment(3)

    expect(request).toHaveBeenNthCalledWith(1, {
      url: '/admin/department',
      method: 'get',
      params: { keyword: 'card', status: 1 }
    })
    expect(request).toHaveBeenNthCalledWith(2, { url: '/admin/department/tree', method: 'get' })
    expect(request).toHaveBeenNthCalledWith(3, { url: '/admin/department/3', method: 'get' })
    expect(request).toHaveBeenNthCalledWith(4, { url: '/admin/department', method: 'post', data })
    expect(request).toHaveBeenNthCalledWith(5, { url: '/admin/department/3', method: 'put', data })
    expect(request).toHaveBeenNthCalledWith(6, { url: '/admin/department/3/toggle-status', method: 'post' })
    expect(request).toHaveBeenNthCalledWith(7, { url: '/admin/department/3', method: 'delete' })
  })

  it('builds doctor requests', async () => {
    const api = await import('@/api/doctor')
    const data = { name: 'Dr A' }

    api.listDoctors({ pageNum: 1 })
    api.checkDoctorDisable(8)
    api.exportDoctors(2)
    api.createDoctor(data)
    api.updateDoctor(8, data)
    api.toggleDoctorStatus(8, true)
    api.toggleDoctorStatus(8, false)
    api.deleteDoctor(8)
    api.listDepartments()
    api.listDoctorRoles()

    expect(request).toHaveBeenNthCalledWith(1, { url: '/admin/doctor/list', method: 'get', params: { pageNum: 1 } })
    expect(request).toHaveBeenNthCalledWith(2, { url: '/admin/doctor/8/disable-check', method: 'get' })
    expect(request).toHaveBeenNthCalledWith(3, { url: '/admin/doctor/export', method: 'get', params: { departmentId: 2 } })
    expect(request).toHaveBeenNthCalledWith(4, { url: '/admin/doctor', method: 'post', data })
    expect(request).toHaveBeenNthCalledWith(5, { url: '/admin/doctor/8', method: 'put', data })
    expect(request).toHaveBeenNthCalledWith(6, { url: '/admin/doctor/8/toggle-status', method: 'patch', params: { force: 'true' } })
    expect(request).toHaveBeenNthCalledWith(7, { url: '/admin/doctor/8/toggle-status', method: 'patch', params: { force: 'false' } })
    expect(request).toHaveBeenNthCalledWith(8, { url: '/admin/doctor/8', method: 'delete' })
    expect(request).toHaveBeenNthCalledWith(9, { url: '/admin/department', method: 'get' })
    expect(request).toHaveBeenNthCalledWith(10, { url: '/admin/doctor/roles', method: 'get' })
  })

  it('builds role requests', async () => {
    const api = await import('@/api/role')
    const data = { roleCode: 'ADMIN' }

    api.listRoles()
    api.getRoleDetail(1)
    api.createRole(data)
    api.updateRole(1, data)
    api.deleteRole(1)
    api.toggleRoleStatus(1)

    expect(request).toHaveBeenNthCalledWith(1, { url: '/admin/roles', method: 'get' })
    expect(request).toHaveBeenNthCalledWith(2, { url: '/admin/roles/1', method: 'get' })
    expect(request).toHaveBeenNthCalledWith(3, { url: '/admin/roles', method: 'post', data })
    expect(request).toHaveBeenNthCalledWith(4, { url: '/admin/roles/1', method: 'put', data })
    expect(request).toHaveBeenNthCalledWith(5, { url: '/admin/roles/1', method: 'delete' })
    expect(request).toHaveBeenNthCalledWith(6, { url: '/admin/roles/1/toggle-status', method: 'patch' })
  })

  it('builds schedule requests', async () => {
    const api = await import('@/api/schedule')
    const data = { doctorId: 1 }

    api.listDepartments()
    api.listDoctors(2)
    api.listSchedules({ startDate: '2026-07-01' })
    api.getScheduleDetail(4)
    api.listScheduleRegistrations(4)
    api.createSchedule(data)
    api.batchCreateSchedule([data])
    api.updateSchedule(4, data)
    api.cancelSchedule(4)
    api.generateScheduleSuggestion(data)
    api.acceptSuggestion(9)
    api.rejectSuggestion(9)
    api.acceptSuggestionDetail(9, 10)
    api.rejectSuggestionDetail(9, 10)

    expect(request).toHaveBeenNthCalledWith(1, { url: '/admin/department', method: 'get' })
    expect(request).toHaveBeenNthCalledWith(2, { url: '/admin/doctors', method: 'get', params: { departmentId: 2 } })
    expect(request).toHaveBeenNthCalledWith(3, { url: '/admin/schedules', method: 'get', params: { startDate: '2026-07-01' } })
    expect(request).toHaveBeenNthCalledWith(4, { url: '/admin/schedules/4', method: 'get' })
    expect(request).toHaveBeenNthCalledWith(5, { url: '/admin/schedules/4/registrations', method: 'get' })
    expect(request).toHaveBeenNthCalledWith(6, { url: '/admin/schedules', method: 'post', data })
    expect(request).toHaveBeenNthCalledWith(7, { url: '/admin/schedules/batch', method: 'post', data: [data] })
    expect(request).toHaveBeenNthCalledWith(8, { url: '/admin/schedules/4', method: 'put', data })
    expect(request).toHaveBeenNthCalledWith(9, { url: '/admin/schedules/4', method: 'delete' })
    expect(request).toHaveBeenNthCalledWith(10, { url: '/admin/schedules/ai-suggestions', method: 'post', data })
    expect(request).toHaveBeenNthCalledWith(11, { url: '/admin/schedules/ai-suggestions/9/accept', method: 'post' })
    expect(request).toHaveBeenNthCalledWith(12, { url: '/admin/schedules/ai-suggestions/9/reject', method: 'post' })
    expect(request).toHaveBeenNthCalledWith(13, { url: '/admin/schedules/ai-suggestions/9/details/10/accept', method: 'post' })
    expect(request).toHaveBeenNthCalledWith(14, { url: '/admin/schedules/ai-suggestions/9/details/10/reject', method: 'post' })
  })
})
