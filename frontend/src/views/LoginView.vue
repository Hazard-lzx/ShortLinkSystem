<template>
  <div class="login-container">
    <div class="blob blob-1" />
    <div class="blob blob-2" />
    <div class="grid-overlay" />

    <div class="login-card">
      <div class="login-header">
        <div class="logo-wrap">
          <img src="/favicon.svg" alt="logo" />
        </div>
        <h1>短链管理系统</h1>
        <p>高并发分布式短链接平台</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button class="login-btn" type="primary" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        还没有账号？
        <router-link to="/register" class="link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login({ ...form })
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch {
    /* 错误已在拦截器中提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="less">
.login-container {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: radial-gradient(1200px 700px at 78% -10%, #3b6fe0 0%, transparent 55%),
    radial-gradient(900px 600px at -10% 110%, #1e40af 0%, transparent 50%),
    linear-gradient(160deg, #16295e 0%, #1d3a8a 45%, #2b5cbb 100%);
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.35;
  pointer-events: none;
  animation: float-slow 9s ease-in-out infinite;

  &.blob-1 {
    width: 420px;
    height: 420px;
    top: -120px;
    right: -80px;
    background: #60a5fa;
  }

  &.blob-2 {
    width: 360px;
    height: 360px;
    bottom: -100px;
    left: -60px;
    background: #38bdf8;
    animation-delay: -4.5s;
  }
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 75%);
  pointer-events: none;
}

.login-card {
  position: relative;
  width: 408px;
  padding: 44px 38px 28px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  box-shadow: 0 24px 64px rgba(9, 20, 55, 0.45);
  backdrop-filter: blur(12px);
  animation: card-in 0.5s cubic-bezier(0.22, 1, 0.36, 1);

  .login-header {
    text-align: center;
    margin-bottom: 30px;

    .logo-wrap {
      width: 64px;
      height: 64px;
      margin: 0 auto;
      border-radius: 18px;
      background: linear-gradient(135deg, #dbeafe, #eff6ff);
      border: 1px solid #e0eaff;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: inset 0 1px 2px #fff, 0 6px 16px rgba(37, 99, 235, 0.18);

      img {
        width: 36px;
        height: 36px;
      }
    }

    h1 {
      margin-top: 16px;
      font-size: 22px;
      font-weight: 700;
      letter-spacing: 0.5px;
      color: var(--text-1);
    }

    p {
      margin-top: 8px;
      font-size: 13px;
      color: var(--text-3);
    }
  }

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 4px 14px;
    box-shadow: 0 0 0 1px #e5e8f0 inset;
    transition: box-shadow 0.2s;

    &:hover {
      box-shadow: 0 0 0 1px #c9d4f5 inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1.5px var(--brand-2) inset;
    }
  }

  .login-btn {
    width: 100%;
    height: 44px;
    border: none;
    border-radius: 10px;
    font-size: 15px;
    font-weight: 600;
    letter-spacing: 6px;
    background: linear-gradient(135deg, var(--brand-1), var(--brand-2) 55%, var(--brand-3));
    box-shadow: 0 8px 20px rgba(37, 99, 235, 0.32);
    transition: transform 0.15s ease, box-shadow 0.2s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 10px 26px rgba(37, 99, 235, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .login-footer {
    text-align: center;
    font-size: 13px;
    color: var(--text-3);

    .link {
      color: var(--brand-2);
      font-weight: 500;
      margin-left: 4px;

      &:hover {
        color: var(--brand-1);
      }
    }
  }
}

@keyframes card-in {
  from {
    opacity: 0;
    transform: translateY(22px) scale(0.98);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
