<template>
  <div class="dashboard">
    <div class="page-header page-card">
      <div class="header-info">
        <div class="title">数据看板</div>
        <div class="subtitle">总览短链与访问数据</div>
      </div>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="icon-wrap blue">
            <el-icon :size="26"><Link /></el-icon>
          </div>
          <div class="stat-info">
            <div class="label">短链总数</div>
            <div class="value">{{ totalLinksDisplay }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="icon-wrap green">
            <el-icon :size="26"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="label">累计访问量</div>
            <div class="value">{{ totalVisitsDisplay }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="icon-wrap orange">
            <el-icon :size="26"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="label">今日访问量</div>
            <div class="value">{{ todayVisitsDisplay }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="16">
        <div class="page-card chart-card">
          <div class="card-header">
            <div>
              <div class="title">访问趋势</div>
              <div class="subtitle">短链跳转量按日统计</div>
            </div>
            <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
              <el-radio-button :value="7">近 7 天</el-radio-button>
              <el-radio-button :value="30">近 30 天</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendChartRef" class="chart trend-chart" v-loading="trendLoading" />
        </div>
      </el-col>
      <el-col :span="8">
        <div class="page-card top-card">
          <div class="card-header">
            <div>
              <div class="title">访问 Top 10</div>
              <div class="subtitle">热度最高的短链</div>
            </div>
          </div>
          <div v-loading="topLoading" class="top-list">
            <template v-if="topList.length">
              <div v-for="(item, index) in topList" :key="item.shortCode" class="top-item">
                <span :class="['rank', `rank-${index + 1}`]">{{ index + 1 }}</span>
                <div class="top-main">
                  <el-tooltip :content="item.originalUrl" placement="top" :show-after="300">
                    <div class="top-link">{{ item.originalUrl }}</div>
                  </el-tooltip>
                  <div class="top-bar-track">
                    <div
                      class="top-bar"
                      :style="{ width: `${barWidth(item.visitCount)}%` }"
                    />
                  </div>
                </div>
                <span class="top-count">{{ item.visitCount }}</span>
              </div>
            </template>
            <el-empty v-else description="暂无访问数据" :image-size="80" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import { getStatsOverview, getStatsTrend, getStatsTop } from '@/api/stats'
import type { StatsOverview, StatsTopItem, StatsTrendItem } from '@/api/types'
import { useCountUp } from '@/composables/useCountUp'

echarts.use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

const overview = ref<StatsOverview | null>(null)
const trendDays = ref(7)
const trendList = ref<StatsTrendItem[]>([])
const topList = ref<StatsTopItem[]>([])
const trendLoading = ref(false)
const topLoading = ref(false)

const totalLinksDisplay = useCountUp(() => overview.value?.totalLinks)
const totalVisitsDisplay = useCountUp(() => overview.value?.totalVisits)
const todayVisitsDisplay = useCountUp(() => overview.value?.todayVisits)

const maxTopVisits = computed(() =>
  topList.value.reduce((acc, cur) => Math.max(acc, cur.visitCount), 0)
)

function barWidth(visitCount: number) {
  if (!maxTopVisits.value) return 0
  return Math.max(6, Math.round((visitCount / maxTopVisits.value) * 100))
}

const trendChartRef = ref<HTMLDivElement>()
let chart: ECharts | null = null

function renderTrend() {
  if (!trendChartRef.value) return
  if (!chart) {
    chart = echarts.init(trendChartRef.value)
  }
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: '#e5e8f0',
      borderWidth: 1,
      padding: [8, 12],
      textStyle: { color: '#4b5563', fontSize: 12 },
      axisPointer: { lineStyle: { color: '#c9d4f5' } }
    },
    grid: { left: 44, right: 20, top: 28, bottom: 30 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendList.value.map((i) => i.statDate.slice(5)),
      axisLine: { lineStyle: { color: '#e5e8f0' } },
      axisTick: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f0f2f7', type: 'dashed' } },
      axisLabel: { color: '#9ca3af', fontSize: 11 }
    },
    series: [
      {
        name: '访问量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        showSymbol: false,
        lineStyle: { width: 2.5, color: '#3b82f6' },
        emphasis: { showSymbol: true, focus: 'series' },
        itemStyle: { color: '#3b82f6', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.28)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.01)' }
          ])
        },
        data: trendList.value.map((i) => i.visitCount)
      }
    ]
  })
}

async function loadOverview() {
  try {
    overview.value = await getStatsOverview()
  } catch {
    /* 拦截器已提示 */
  }
}

async function loadTrend() {
  trendLoading.value = true
  try {
    trendList.value = await getStatsTrend(trendDays.value)
    renderTrend()
  } catch {
    /* 拦截器已提示 */
  } finally {
    trendLoading.value = false
  }
}

async function loadTop() {
  topLoading.value = true
  try {
    topList.value = await getStatsTop(10)
  } catch {
    /* 拦截器已提示 */
  } finally {
    topLoading.value = false
  }
}

function handleResize() {
  chart?.resize()
}

onMounted(() => {
  loadOverview()
  loadTrend()
  loadTop()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped lang="less">
.dashboard {
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 18px 24px;
    margin-bottom: 16px;

    .header-info {
      .title {
        font-size: 18px;
        font-weight: 700;
        color: var(--text-1);
        letter-spacing: 0.3px;
      }

      .subtitle {
        margin-top: 5px;
        font-size: 13px;
        color: var(--text-3);
      }
    }
  }

  .stat-row {
    margin-bottom: 16px;
  }

  .stat-card {
    background: #fff;
    border-radius: var(--card-radius);
    border: var(--card-border);
    box-shadow: var(--card-shadow);
    padding: 22px 24px;
    display: flex;
    align-items: center;
    gap: 18px;
    transition: box-shadow 0.25s ease, transform 0.25s ease;

    &:hover {
      transform: translateY(-3px);
      box-shadow: var(--card-shadow-hover);
    }

    .icon-wrap {
      flex-shrink: 0;
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;

      &.blue {
        background: linear-gradient(135deg, #3b82f6, #60a5fa);
        box-shadow: 0 6px 14px rgba(59, 130, 246, 0.32);
      }

      &.green {
        background: linear-gradient(135deg, #10b981, #34d399);
        box-shadow: 0 6px 14px rgba(16, 185, 129, 0.3);
      }

      &.orange {
        background: linear-gradient(135deg, #f59e0b, #fbbf24);
        box-shadow: 0 6px 14px rgba(245, 158, 11, 0.3);
      }
    }

    .stat-info {
      min-width: 0;

      .label {
        font-size: 13px;
        color: var(--text-3);
      }

      .value {
        margin-top: 6px;
        font-size: 28px;
        font-weight: 700;
        line-height: 1.1;
        color: var(--text-1);
        font-variant-numeric: tabular-nums;
      }
    }
  }

  .chart-row {
    .card-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      margin-bottom: 6px;
      padding: 14px 16px 0;

      .title {
        font-size: 15px;
        font-weight: 600;
        color: var(--text-1);
      }

      .subtitle {
        margin-top: 4px;
        font-size: 12px;
        color: var(--text-3);
      }
    }

    .trend-chart {
      height: 372px;
    }

    .chart-card,
    .top-card {
      height: 100%;
    }

    .top-list {
      height: 380px;
      overflow-y: auto;
      padding: 10px 16px 14px;
      display: flex;
      flex-direction: column;
      justify-content: flex-start;

      .top-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 9px 0;
        border-bottom: 1px dashed #eef0f5;

        &:last-child {
          border-bottom: none;
        }

        .rank {
          flex-shrink: 0;
          width: 22px;
          height: 22px;
          border-radius: 7px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          font-weight: 600;
          color: var(--text-3);
          background: #f2f4f8;

          &.rank-1 {
            background: linear-gradient(135deg, #ef4444, #f97316);
            color: #fff;
          }

          &.rank-2 {
            background: linear-gradient(135deg, #f59e0b, #fbbf24);
            color: #fff;
          }

          &.rank-3 {
            background: linear-gradient(135deg, #3b82f6, #60a5fa);
            color: #fff;
          }
        }

        .top-main {
          flex: 1;
          min-width: 0;

          .top-link {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            font-size: 12.5px;
            color: var(--text-2);
            margin-bottom: 5px;
          }

          .top-bar-track {
            height: 5px;
            border-radius: 4px;
            background: #f0f2f7;
            overflow: hidden;

            .top-bar {
              height: 100%;
              border-radius: 4px;
              background: linear-gradient(90deg, #93c5fd, #3b82f6);
              transition: width 0.6s cubic-bezier(0.22, 1, 0.36, 1);
            }
          }
        }

        .top-count {
          flex-shrink: 0;
          font-size: 13px;
          font-weight: 700;
          color: var(--brand-2);
          font-variant-numeric: tabular-nums;
        }
      }
    }
  }
}
</style>
