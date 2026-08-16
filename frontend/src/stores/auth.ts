import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/user'
import type { LoginReq, UserInfo } from '@/api/types'
import { storage } from '@/utils/storage'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(storage.getToken() ?? '')
  const username = ref<string>(storage.getUsername())
  const userInfo = ref<UserInfo | null>(null)

  async function login(payload: LoginReq) {
    const data = await loginApi(payload)
    token.value = data.token
    username.value = data.username
    storage.setToken(data.token)
    storage.setUsername(data.username)
  }

  async function fetchUserInfo() {
    userInfo.value = await getUserInfo()
    return userInfo.value
  }

  function logout() {
    token.value = ''
    username.value = ''
    userInfo.value = null
    storage.clearToken()
    storage.clearUsername()
  }

  return { token, username, userInfo, login, logout, fetchUserInfo }
})
