import { get, post } from './request'
import type { LoginReq, LoginResp, RegisterReq, UserInfo } from './types'

export function login(data: LoginReq) {
  return post<LoginResp>('/api/admin/user/login', data)
}

export function register(data: RegisterReq) {
  return post<void>('/api/admin/user/register', data)
}

export function getUserInfo() {
  return get<UserInfo>('/api/admin/user/info')
}
