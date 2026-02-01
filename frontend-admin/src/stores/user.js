import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '../api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const isLoggedIn = ref(!!token.value)

  // 登录
  async function login(username, password) {
    const res = await authApi.login({ username, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    isLoggedIn.value = true
    await fetchUserInfo()
    return res
  }

  // 注册
  async function register(data) {
    return await authApi.register(data)
  }

  // 获取用户信息
  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await authApi.getInfo()
      userInfo.value = res.data
    } catch (e) {
      console.error('获取用户信息失败:', e)
      // token 无效时清除登录状态
      logout()
    }
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
  }

  // 是否是管理员
  function isAdmin() {
    return userInfo.value?.role === 1
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    fetchUserInfo,
    logout,
    isAdmin
  }
})
