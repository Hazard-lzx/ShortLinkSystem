export interface Result<T = unknown> {
  code: number
  message: string
  data: T
  success: boolean
  timestamp: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export type LinkStatus = 0 | 1

export const STATUS_ENABLE = 0 as const
export const STATUS_DISABLE = 1 as const

export interface LoginReq {
  username: string
  password: string
}

export interface LoginResp {
  token: string
  username: string
  expireHour: number
}

export interface RegisterReq {
  username: string
  password: string
  phone?: string
}

export interface UserInfo {
  id: number
  username: string
  phone: string | null
  status: number
  createTime: string
}

export interface LinkCreateReq {
  originalUrl: string
  expireTime?: string
}

export interface LinkUpdateReq {
  shortCode: string
  originalUrl: string
  expireTime?: string
}

export interface LinkItem {
  id: number
  shortCode: string
  originalUrl: string
  expireTime: string | null
  status: LinkStatus
  visitCount: number
  createTime: string
  updateTime: string
}

export interface LinkPageReq {
  current?: number
  size?: number
  shortCode?: string
  originalUrl?: string
  status?: LinkStatus
}

export interface StatsOverview {
  totalLinks: number
  totalVisits: number
  todayVisits: number
}

export interface StatsTrendItem {
  statDate: string
  visitCount: number
}

export interface StatsTopItem {
  shortCode: string
  originalUrl: string
  visitCount: number
}
