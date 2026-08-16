<template>
  <el-container class="layout">
    <el-aside :width="isCollapse ? '64px' : '216px'" class="aside">
      <div class="brand">
        <div class="brand-logo">
          <img src="/favicon.svg" alt="logo" />
        </div>
        <transition name="fade-slide">
          <span v-if="!isCollapse" class="brand-name">短链管理系统</span>
        </transition>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="transparent"
        text-color="rgba(255, 255, 255, 0.66)"
        active-text-color="#ffffff"
        class="side-menu"
      >
        <el-menu-item index="/create">
          <el-icon><Position /></el-icon>
          <template #title>创建短链</template>
        </el-menu-item>
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据看板</template>
        </el-menu-item>
        <el-menu-item index="/links">
          <el-icon><Link /></el-icon>
          <template #title>短链管理</template>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><UserFilled /></el-icon>
          <template #title>个人信息</template>
        </el-menu-item>
      </el-menu>

      <div v-if="!isCollapse" class="side-footer">ShortLink v1.0</div>
    </el-aside>

    <el-container class="body-container">
      <el-header class="header">
        <div class="header-left">
          <div class="collapse-btn" @click="isCollapse = !isCollapse">
            <el-icon :size="18">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.path !== '/dashboard'">
              {{ route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" class="avatar">
                {{ authStore.username.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="name">{{ authStore.username }}</span>
              <el-icon :size="12" color="#9ca3af"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isCollapse = ref(false)

function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        authStore.logout()
        router.push('/login')
      })
      .catch(() => {})
  }
}
</script>

<style scoped lang="less">
.layout {
  height: 100%;
}

.aside {
  position: relative;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #101a33 0%, #0d1730 60%, #0b142a 100%);
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    width: 1px;
    background: linear-gradient(180deg, transparent, rgba(255, 255, 255, 0.08), transparent);
  }

  .brand {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    flex-shrink: 0;

    .brand-logo {
      width: 34px;
      height: 34px;
      border-radius: 10px;
      background: linear-gradient(135deg, #2563eb, #60a5fa);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(37, 99, 235, 0.4);

      img {
        width: 22px;
        height: 22px;
      }
    }

    .brand-name {
      color: #fff;
      font-size: 15.5px;
      font-weight: 600;
      letter-spacing: 0.5px;
      white-space: nowrap;
    }
  }

  .side-menu {
    padding: 8px;
    border-right: none;
    flex: 1;

    :deep(.el-menu-item) {
      height: 46px;
      margin: 4px 0;
      border-radius: 10px;
      transition: all 0.2s ease;

      .el-icon {
        font-size: 17px;
      }

      &:hover {
        background: rgba(255, 255, 255, 0.07);
        color: #fff;
      }

      &.is-active {
        background: linear-gradient(135deg, var(--brand-1), var(--brand-2));
        color: #fff;
        box-shadow: 0 4px 14px rgba(37, 99, 235, 0.4);
      }
    }
  }

  .side-footer {
    padding: 14px 0;
    text-align: center;
    font-size: 11px;
    color: rgba(255, 255, 255, 0.28);
    letter-spacing: 1px;
    flex-shrink: 0;
  }
}

.body-container {
  min-width: 0;
}

.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px 0 16px;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #eef0f5;
  position: relative;
  z-index: 10;

  .header-left {
    display: flex;
    align-items: center;
    gap: 14px;

    .collapse-btn {
      width: 36px;
      height: 36px;
      border-radius: 9px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      color: var(--text-2);
      transition: all 0.2s;

      &:hover {
        background: #eef2fb;
        color: var(--brand-2);
      }
    }

    :deep(.el-breadcrumb__inner) {
      font-weight: 500;
      color: var(--text-2);
    }

    :deep(.el-breadcrumb__inner.is-link:hover) {
      color: var(--brand-2);
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      padding: 5px 10px;
      border-radius: 10px;
      transition: background 0.2s;
      outline: none;

      &:hover {
        background: #f2f5fb;
      }

      .avatar {
        background: linear-gradient(135deg, #2563eb, #60a5fa);
        font-weight: 600;
        box-shadow: 0 3px 8px rgba(37, 99, 235, 0.3);
      }

      .name {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-1);
      }
    }
  }
}

.main {
  background: var(--bg-page);
  padding: 20px;
}
</style>
