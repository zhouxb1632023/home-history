import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import http from '@/api/http'

function safeParseJson(str) {
  try {
    return JSON.parse(str)
  } catch {
    return {}
  }
}

export const useUserStore = defineStore('user', () => {
  const saved = safeParseJson(localStorage.getItem('auth_user') || '{}')
  const token = ref(localStorage.getItem('auth_token') || '')
  const nickname = ref(saved.nickname || '')
  const avatarUrl = ref(saved.avatarUrl || '')

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data) {
    token.value = data.token
    const name = data.nickname || data.username || ''
    nickname.value = name
    avatarUrl.value = data.avatarUrl || ''
    localStorage.setItem('auth_token', data.token)
    localStorage.setItem('auth_user', JSON.stringify({ nickname: name, avatarUrl: avatarUrl.value }))
  }

  function clearAuth() {
    token.value = ''
    nickname.value = ''
    avatarUrl.value = ''
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
  }

  async function updateProfile(newNickname) {
    const { data } = await http.put('/auth/profile', { nickname: newNickname })
    nickname.value = data.nickname
    avatarUrl.value = data.avatarUrl || ''
    localStorage.setItem('auth_user', JSON.stringify({ nickname: data.nickname, avatarUrl: avatarUrl.value }))
  }

  async function uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    const { data } = await http.post('/auth/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    avatarUrl.value = data.avatarUrl || ''
    localStorage.setItem('auth_user', JSON.stringify({ nickname: nickname.value, avatarUrl: avatarUrl.value }))
  }

  async function fetchProfile() {
    try {
      const { data } = await http.get('/auth/me')
      const name = data.nickname || data.username || nickname.value
      nickname.value = name
      avatarUrl.value = data.avatarUrl || avatarUrl.value
      if (data.token) {
        token.value = data.token
        localStorage.setItem('auth_token', data.token)
      }
      localStorage.setItem('auth_user', JSON.stringify({ nickname: name, avatarUrl: avatarUrl.value }))
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  }

  return { token, nickname, avatarUrl, isLoggedIn, setAuth, clearAuth, updateProfile, uploadAvatar, fetchProfile }
})