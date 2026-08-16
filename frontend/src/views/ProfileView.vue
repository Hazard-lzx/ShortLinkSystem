<template>
  <div class="profile-wrap">
    <div class="profile page-card">
      <div class="profile-header">
        <div class="avatar-ring">
          <el-avatar :size="76" class="avatar">
            {{ (userInfo?.username ?? authStore.username).charAt(0).toUpperCase() }}
          </el-avatar>
        </div>
        <div class="profile-title">
          <h2>{{ userInfo?.username ?? authStore.username }}</h2>
          <el-tag :type="userInfo?.status === 0 ? 'success' : 'danger'" size="small" effect="light" round>
            {{ userInfo?.status === 0 ? '账号正常' : '已禁用' }}
          </el-tag>
        </div>
      </div>

      <div v-loading="loading" class="profile-desc">
        <div class="desc-item">
          <div class="desc-label">
            <el-icon><User /></el-icon>用户 ID
          </div>
          <div class="desc-value mono">{{ userInfo?.id ?? '-' }}</div>
        </div>
        <div class="desc-item">
          <div class="desc-label">
            <el-icon><EditPen /></el-icon>用户名
          </div>
          <div class="desc-value">{{ userInfo?.username ?? '-' }}</div>
        </div>
        <div class="desc-item">
          <div class="desc-label">
            <el-icon><Iphone /></el-icon>手机号
          </div>
          <div class="desc-value">{{ userInfo?.phone || '未绑定' }}</div>
        </div>
        <div class="desc-item">
          <div class="desc-label">
            <el-icon><Clock /></el-icon>注册时间
          </div>
          <div class="desc-value">{{ userInfo?.createTime ?? '-' }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { User, EditPen, Iphone, Clock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { UserInfo } from '@/api/types'

const authStore = useAuthStore()
const userInfo = ref<UserInfo | null>(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    userInfo.value = await authStore.fetchUserInfo()
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="less">
.profile-wrap {
  display: flex;
  justify-content: center;
}

.profile {
  width: 100%;
  max-width: 720px;
  padding: 28px;

  .profile-header {
    display: flex;
    align-items: center;
    gap: 20px;
    padding-bottom: 24px;
    border-bottom: 1px solid #eef0f5;

    .avatar-ring {
      padding: 4px;
      border-radius: 50%;
      background: linear-gradient(135deg, #93c5fd, #3b82f6);
      box-shadow: 0 8px 20px rgba(37, 99, 235, 0.25);

      .avatar {
        background: linear-gradient(135deg, #2563eb, #60a5fa);
        font-size: 30px;
        font-weight: 700;
        border: 3px solid #fff;
      }
    }

    .profile-title {
      display: flex;
      align-items: center;
      gap: 12px;

      h2 {
        font-size: 21px;
        font-weight: 700;
        color: var(--text-1);
      }
    }
  }

  .profile-desc {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 14px;
    padding-top: 22px;
    min-height: 140px;

    .desc-item {
      padding: 16px 18px;
      border-radius: 12px;
      background: #f8fafd;
      border: 1px solid #eef0f5;
      transition: all 0.2s;

      &:hover {
        border-color: #dbe7ff;
        background: #f5f8ff;
        transform: translateY(-2px);
      }

      .desc-label {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12.5px;
        color: var(--text-3);

        .el-icon {
          font-size: 14px;
        }
      }

      .desc-value {
        margin-top: 8px;
        font-size: 15px;
        font-weight: 600;
        color: var(--text-1);

        &.mono {
          font-family: 'JetBrains Mono', Consolas, monospace;
        }
      }
    }
  }
}
</style>
