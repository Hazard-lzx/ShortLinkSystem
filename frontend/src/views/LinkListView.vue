<template>
  <div class="link-list page-card">
    <div class="toolbar">
      <el-form :inline="true" :model="query" @submit.prevent="handleSearch">
        <el-form-item label="短码">
          <el-input v-model="query.shortCode" placeholder="精确查询" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="原始链接">
          <el-input v-model="query.originalUrl" placeholder="模糊查询" clearable style="width: 210px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="启用" :value="0" />
            <el-option label="禁用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="list" class="link-table">
      <el-table-column label="短码" width="150">
        <template #default="{ row }">
          <el-tooltip content="点击访问短链" placement="top" :show-after="400">
            <span class="short-code" @click="openShortLink(row.shortCode)">
              {{ row.shortCode }}
            </span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="原始链接" min-width="260" show-overflow-tooltip>
        <template #default="{ row }">
          <a :href="row.originalUrl" target="_blank" rel="noopener" class="origin-link">
            {{ row.originalUrl }}
          </a>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" effect="light" round>
            {{ row.status === 0 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="有效期" width="180">
        <template #default="{ row }">
          <span :class="['expire', { expired: isExpired(row) }]">
            {{ row.expireTime ?? '永久有效' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="访问量" width="100" sortable prop="visitCount">
        <template #default="{ row }">
          <span class="visit-count">{{ row.visitCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170" prop="createTime">
        <template #default="{ row }">
          <span class="dim">{{ row.createTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="CopyDocument" @click="copyShortLink(row.shortCode)">
            复制
          </el-button>
          <el-divider direction="vertical" />
          <el-button link type="primary" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button
            link
            :type="row.status === 0 ? 'warning' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 0 ? '禁用' : '启用' }}
          </el-button>
          <el-divider direction="vertical" />
          <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无短链数据，可通过左侧「创建短链」页面新建" />
      </template>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadList"
        @current-change="loadList"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="编辑短链"
      width="520px"
      destroy-on-close
      class="link-dialog"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="原始链接" prop="originalUrl">
          <el-input
            v-model="form.originalUrl"
            placeholder="https://example.com/very/long/url"
            clearable
          />
        </el-form-item>
        <el-form-item label="过期时间" prop="expireTime">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            placeholder="不选则为永久有效"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            :disabled-date="(date: Date) => date.getTime() < Date.now() - 86400000"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Search, Refresh, Edit, Delete, CopyDocument } from '@element-plus/icons-vue'
import { deleteLink, pageLinks, updateLink, updateLinkStatus } from '@/api/link'
import type { LinkItem, LinkStatus } from '@/api/types'

const loading = ref(false)
const submitting = ref(false)
const list = ref<LinkItem[]>([])

const query = reactive({
  shortCode: '',
  originalUrl: '',
  status: undefined as LinkStatus | undefined
})

const page = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const editingCode = ref('')
const formRef = ref<FormInstance>()
const form = reactive({ originalUrl: '', expireTime: '' })

const formRules: FormRules = {
  originalUrl: [
    { required: true, message: '请输入原始链接', trigger: 'blur' },
    { pattern: /^https?:\/\/.+/, message: '链接须以 http:// 或 https:// 开头', trigger: 'blur' },
    { max: 1024, message: '链接长度不能超过 1024 字符', trigger: 'blur' }
  ]
}

function fullShortLink(code: string) {
  return `${window.location.origin}/s/${code}`
}

function openShortLink(code: string) {
  window.open(`/s/${code}`, '_blank', 'noopener')
}

async function copyShortLink(code: string) {
  try {
    await navigator.clipboard.writeText(fullShortLink(code))
    ElMessage.success('短链已复制到剪贴板')
  } catch {
    const input = document.createElement('input')
    input.value = fullShortLink(code)
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ElMessage.success('短链已复制到剪贴板')
  }
}

function isExpired(row: LinkItem) {
  return !!row.expireTime && new Date(row.expireTime.replace(/-/g, '/')).getTime() < Date.now()
}

async function loadList() {
  loading.value = true
  try {
    const data = await pageLinks({
      current: page.current,
      size: page.size,
      shortCode: query.shortCode || undefined,
      originalUrl: query.originalUrl || undefined,
      status: query.status
    })
    list.value = data.records
    page.total = data.total
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.current = 1
  loadList()
}

function handleReset() {
  query.shortCode = ''
  query.originalUrl = ''
  query.status = undefined
  handleSearch()
}

function openEditDialog(row: LinkItem) {
  editingCode.value = row.shortCode
  form.originalUrl = row.originalUrl
  form.expireTime = row.expireTime ?? ''
  dialogVisible.value = true
}

function resetForm() {
  form.originalUrl = ''
  form.expireTime = ''
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await updateLink({
      shortCode: editingCode.value,
      originalUrl: form.originalUrl,
      expireTime: form.expireTime || undefined
    })
    ElMessage.success('修改成功')
    dialogVisible.value = false
    loadList()
  } catch {
    /* 拦截器已提示 */
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row: LinkItem) {
  const target: LinkStatus = row.status === 0 ? 1 : 0
  const action = target === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}短链「${row.shortCode}」吗？`, '提示', {
      confirmButtonText: action,
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateLinkStatus(row.shortCode, target)
    ElMessage.success(`${action}成功`)
    loadList()
  } catch {
    /* 用户取消或拦截器已提示 */
  }
}

async function handleDelete(row: LinkItem) {
  try {
    await ElMessageBox.confirm(
      `删除后短链「${row.shortCode}」将无法访问，确定删除吗？`,
      '删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteLink(row.shortCode)
    ElMessage.success('删除成功')
    if (list.value.length === 1 && page.current > 1) {
      page.current -= 1
    }
    loadList()
  } catch {
    /* 用户取消或拦截器已提示 */
  }
}

onMounted(loadList)
</script>

<style scoped lang="less">
.link-list {
  padding: 18px;

  .toolbar {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 6px;

    :deep(.el-form-item) {
      margin-bottom: 12px;
      margin-right: 14px;
    }

    :deep(.el-form-item__label) {
      color: var(--text-2);
      font-weight: 500;
    }
  }

  .link-table {
    border-radius: 10px;

    :deep(thead th) {
      background: #f8fafd;
      color: var(--text-2);
      font-weight: 600;
      font-size: 13px;
    }

    :deep(td) {
      font-size: 13.5px;
    }

    :deep(tr) {
      transition: background 0.15s ease;
    }
  }

  .short-code {
    display: inline-block;
    padding: 3px 10px;
    border-radius: 7px;
    background: #eef4ff;
    border: 1px solid #dbe7ff;
    color: var(--brand-1);
    font-size: 13px;
    font-weight: 600;
    font-family: 'JetBrains Mono', Consolas, monospace;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #e0ecff;
      border-color: #c5dcff;
      box-shadow: 0 2px 8px rgba(37, 99, 235, 0.18);
    }
  }

  .origin-link {
    color: var(--text-2);
    text-decoration: none;

    &:hover {
      color: var(--brand-2);
    }
  }

  .expire {
    color: var(--text-2);
    font-size: 13px;

    &.expired {
      color: #ef4444;
      font-weight: 600;
    }
  }

  .dim {
    color: var(--text-3);
    font-size: 13px;
  }

  .visit-count {
    font-weight: 700;
    color: var(--brand-2);
    font-variant-numeric: tabular-nums;
  }

  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
