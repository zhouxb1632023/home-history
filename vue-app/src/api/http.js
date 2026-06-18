import axios from "axios"
import { API_BASE } from "./env"

const http = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
})

// Use dynamic imports to avoid circular dependency
http.interceptors.request.use(
  async (config) => {
    const { useUserStore } = await import("@/stores/user.js")
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const { useUserStore } = await import("@/stores/user.js")
      const { useRouter } = await import("vue-router")
      const userStore = useUserStore()
      const router = useRouter()
      userStore.clearAuth()
      router.push("/login")
    }
    return Promise.reject(error)
  }
)

export default http