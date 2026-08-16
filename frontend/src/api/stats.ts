import { get } from './request'
import type { StatsOverview, StatsTopItem, StatsTrendItem } from './types'

export function getStatsOverview() {
  return get<StatsOverview>('/api/admin/stats/overview')
}

export function getStatsTrend(days = 7) {
  return get<StatsTrendItem[]>('/api/admin/stats/trend', { days })
}

export function getStatsTop(limit = 10) {
  return get<StatsTopItem[]>('/api/admin/stats/top', { limit })
}
