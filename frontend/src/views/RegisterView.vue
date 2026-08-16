<template>
  <div class="register-container">
    <div class="blob blob-1" />
    <div class="blob blob-2" />
    <div class="grid-overlay" />

    <div class="register-card">
      <div class="register-header">
        <div class="logo-wrap">
          <img src="/favicon.svg" alt="logo" />
        </div>
        <h1>注册账号</h1>
        <p>加入短链管理系统</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3-64位）" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码（6-32位）"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（选填）" :prefix-icon="Iphone" clearable />
        </el-form-item>
        <el-form-item>
          <el-button class="register-btn" type="primary" :loading="loading" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        已有账号？
        <router-link to="/login" class="link">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Iphone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度需在 3-64 位之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在 6-32 位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '手机号格式不正确',
      trigger: 'blur'
    }
  ]
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      phone: form.phone || undefined
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    /* 错误已在拦截器中提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="less">
.register-container {
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

.register-card {
  position: relative;
  width: 408px;
  padding: 40px 38px 26px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  box-shadow: 0 24px 64px rgba(9, 20, 55, 0.45);
  backdrop-filter: blur(12px);
  animation: card-in 0.5s cubic-bezier(0.22, 1, 0.36, 1);

  .register-header {
    text-align: center;
    margin-bottom: 24px;

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

  .register-btn {
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

  .register-footer {
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
