<template>
  <div class="create-view">
    <div class="page-header page-card">
      <div class="header-info">
        <div class="title">创建短链</div>
        <div class="subtitle">粘贴长链接，一键生成更简短、更易分享的短链接</div>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="15">
        <div class="page-card form-card">
          <template v-if="!createdLink">
            <el-form
              ref="formRef"
              :model="form"
              :rules="formRules"
              label-position="top"
              size="large"
              @submit.prevent="handleSubmit"
            >
              <el-form-item label="原始链接" prop="originalUrl">
                <el-input
                  v-model="form.originalUrl"
                  placeholder="https://example.com/very/long/url"
                  clearable
                />
              </el-form-item>
              <el-form-item label="过期时间（选填）" prop="expireTime">
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
              <el-button
                class="submit-btn"
                type="primary"
                size="large"
                :loading="submitting"
                :icon="Position"
                @click="handleSubmit"
              >
                生成短链
              </el-button>
            </el-form>
          </template>

          <div v-else class="result">
            <div class="result-icon">
              <el-icon :size="38" color="#fff"><CircleCheckFilled /></el-icon>
            </div>
            <div class="result-title">短链创建成功</div>
            <div class="result-link-box">
              <span class="result-link">{{ fullShortLink(createdLink.shortCode) }}</span>
              <el-button
                link
                type="primary"
                :icon="CopyDocument"
                class="copy-btn"
                @click="copyShortLink(createdLink.shortCode)"
              >
                复制
              </el-button>
            </div>
            <div class="result-meta">
              <div class="meta-item">
                <span class="meta-label">原始链接</span>
                <span class="meta-value">{{ createdLink.originalUrl }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">有效期</span>
                <span class="meta-value">{{ createdLink.expireTime ?? '永久有效' }}</span>
              </div>
            </div>
            <div class="result-actions">
              <el-button type="primary" :icon="RefreshRight" @click="resetCreate">再创建一条</el-button>
              <el-button :icon="Link" @click="openShortLink(createdLink.shortCode)">测试跳转</el-button>
              <el-button :icon="List" @click="router.push('/links')">前往管理</el-button>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="9">
        <div class="page-card tips-card">
          <div class="tips-title">
            <el-icon><InfoFilled /></el-icon>
            使用提示
          </div>
          <div class="tips-list">
            <div class="tip">
              <span class="tip-num">1</span>
              <div>
                <div class="tip-name">支持 http / https</div>
                <div class="tip-desc">链接须以 http:// 或 https:// 开头，最长 1024 字符</div>
              </div>
            </div>
            <div class="tip">
              <span class="tip-num">2</span>
              <div>
                <div class="tip-name">可选过期时间</div>
                <div class="tip-desc">到期后短链自动失效，不选则永久有效</div>
              </div>
            </div>
            <div class="tip">
              <span class="tip-num">3</span>
              <div>
                <div class="tip-name">访问统计</div>
                <div class="tip-desc">每次跳转都会计入访问量，可在数据看板查看趋势</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Position,
  CopyDocument,
  CircleCheckFilled,
  RefreshRight,
  Link,
  List,
  InfoFilled
} from '@element-plus/icons-vue'
import { createLink } from '@/api/link'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const createdLink = ref<{ shortCode: string; originalUrl: string; expireTime: string } | null>(null)

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

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const data = await createLink({
      originalUrl: form.originalUrl,
      expireTime: form.expireTime || undefined
    })
    createdLink.value = {
      shortCode: data.shortCode,
      originalUrl: form.originalUrl,
      expireTime: form.expireTime
    }
  } catch {
    /* 拦截器已提示 */
  } finally {
    submitting.value = false
  }
}

function resetCreate() {
  createdLink.value = null
  form.originalUrl = ''
  form.expireTime = ''
  formRef.value?.clearValidate()
}
</script>

<style scoped lang="less">
.create-view {
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

  .form-card {
    padding: 28px;

    :deep(.el-form-item__label) {
      font-weight: 600;
      color: var(--text-2);
      font-size: 13.5px;
    }

    :deep(.el-input__wrapper),
    :deep(.el-select__wrapper) {
      border-radius: 10px;
    }

    .submit-btn {
      width: 100%;
      height: 46px;
      margin-top: 8px;
      border: none;
      border-radius: 10px;
      font-size: 15px;
      font-weight: 600;
      letter-spacing: 2px;
      background: linear-gradient(135deg, var(--brand-1), var(--brand-2));
      box-shadow: 0 8px 20px rgba(37, 99, 235, 0.32);
      transition: transform 0.15s ease, box-shadow 0.2s ease;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 10px 26px rgba(37, 99, 235, 0.4);
      }
    }
  }

  .result {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 26px 10px 12px;

    .result-icon {
      width: 74px;
      height: 74px;
      border-radius: 50%;
      background: linear-gradient(135deg, #10b981, #34d399);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 10px 24px rgba(16, 185, 129, 0.35);
      animation: pop-in 0.45s cubic-bezier(0.22, 1.4, 0.36, 1);
    }

    .result-title {
      margin-top: 18px;
      font-size: 19px;
      font-weight: 700;
      color: var(--text-1);
    }

    .result-link-box {
      margin-top: 14px;
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 16px;
      border-radius: 12px;
      background: #f5f8ff;
      border: 1px dashed #bcd3ff;
      max-width: 100%;

      .result-link {
        font-family: 'JetBrains Mono', Consolas, monospace;
        font-size: 15px;
        font-weight: 700;
        color: var(--brand-1);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .result-meta {
      margin-top: 18px;
      width: 100%;
      display: flex;
      flex-direction: column;
      gap: 10px;
      padding: 14px 18px;
      border-radius: 12px;
      background: #f8fafd;
      border: 1px solid #eef0f5;

      .meta-item {
        display: flex;
        gap: 12px;
        font-size: 13px;

        .meta-label {
          flex-shrink: 0;
          color: var(--text-3);
        }

        .meta-value {
          color: var(--text-2);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .result-actions {
      margin-top: 22px;
      display: flex;
      gap: 4px;

      :deep(.el-button) {
        border-radius: 10px;
        font-weight: 500;
      }
    }
  }

  .tips-card {
    padding: 22px 24px;

    .tips-title {
      display: flex;
      align-items: center;
      gap: 7px;
      font-size: 14.5px;
      font-weight: 700;
      color: var(--text-1);
      padding-bottom: 14px;
      border-bottom: 1px solid #eef0f5;

      .el-icon {
        color: var(--brand-2);
        font-size: 17px;
      }
    }

    .tips-list {
      padding-top: 8px;

      .tip {
        display: flex;
        gap: 12px;
        padding: 12px 0;

        .tip-num {
          flex-shrink: 0;
          width: 24px;
          height: 24px;
          margin-top: 2px;
          border-radius: 8px;
          background: #eef4ff;
          border: 1px solid #dbe7ff;
          color: var(--brand-1);
          font-size: 12.5px;
          font-weight: 700;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .tip-name {
          font-size: 13.5px;
          font-weight: 600;
          color: var(--text-1);
        }

        .tip-desc {
          margin-top: 4px;
          font-size: 12.5px;
          color: var(--text-3);
          line-height: 1.6;
        }
      }
    }
  }
}

@keyframes pop-in {
  from {
    opacity: 0;
    transform: scale(0.5);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
