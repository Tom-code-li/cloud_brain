import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function getAdminInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}