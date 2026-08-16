import { get, post, put, del } from './request'
import type { LinkCreateReq, LinkItem, LinkPageReq, LinkUpdateReq, PageResult, LinkStatus } from './types'

export function createLink(data: LinkCreateReq) {
  return post<{ shortCode: string }>('/api/admin/link', data)
}

export function updateLink(data: LinkUpdateReq) {
  return put<void>('/api/admin/link', data)
}

export function updateLinkStatus(shortCode: string, status: LinkStatus) {
  return put<void>(`/api/admin/link/${shortCode}/status/${status}`)
}

export function deleteLink(shortCode: string) {
  return del<void>(`/api/admin/link/${shortCode}`)
}

export function getLinkDetail(shortCode: string) {
  return get<LinkItem>(`/api/admin/link/${shortCode}`)
}

export function pageLinks(params: LinkPageReq) {
  return get<PageResult<LinkItem>>('/api/admin/link/page', params)
}
