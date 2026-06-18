import { defineStore } from 'pinia'
import api from '../utils/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    token: localStorage.getItem('token') || ''
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    avatarUrl: (state) => state.user?.avatarUrl || ''
  },
  actions: {
    async login(username, password) {
      const { data } = await api.post('/auth/login', { username, password })
      this.token = data.token
      this.user = { username: data.username, nickname: data.nickname, avatarUrl: data.avatarUrl, role: data.role }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    async register(username, password, nickname) {
      const { data } = await api.post('/auth/register', { username, password, nickname })
      this.token = data.token
      this.user = { username: data.username, nickname: data.nickname, avatarUrl: data.avatarUrl, role: data.role }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    async fetchProfile() {
      try {
        const { data } = await api.get('/auth/me')
        this.token = data.token
        this.user = { username: data.username, nickname: data.nickname, avatarUrl: data.avatarUrl, role: data.role }
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(this.user))
      } catch (e) {
        console.error('获取用户信息失败', e)
      }
    },
    async updateProfile(nickname) {
      const { data } = await api.put('/auth/profile', { nickname })
      this.user = { ...this.user, nickname: data.nickname, avatarUrl: data.avatarUrl }
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    async uploadAvatar(file) {
      const formData = new FormData()
      formData.append('file', file)
      const { data } = await api.post('/auth/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      this.user = { ...this.user, avatarUrl: data.avatarUrl }
      localStorage.setItem('user', JSON.stringify(this.user))
      return data.avatarUrl
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
