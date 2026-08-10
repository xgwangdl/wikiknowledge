import { defineStore } from 'pinia'
import http from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isLoggedIn: (state) => !!state.accessToken,
    isAdmin: (state) => state.user?.role === 'ROLE_ADMIN'
  },
  actions: {
    async login(payload) {
      const { data } = await http.post('/auth/login', payload)
      this.setSession(data)
    },
    async register(payload) {
      await http.post('/auth/register', payload)
    },
    setSession(data) {
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.user = data.user
      localStorage.setItem('accessToken', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      localStorage.setItem('user', JSON.stringify(data.user))
    },
    logout() {
      this.accessToken = ''
      this.refreshToken = ''
      this.user = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    }
  }
})
