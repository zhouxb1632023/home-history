<template>
  <div class="min-h-screen flex items-center justify-center bg-warm-cream p-4">
    <div class="w-full max-w-md p-8 bg-white rounded-3xl shadow-lg border-2 border-dashed border-warm-peach">
      <div class="text-center mb-8">
        <div class="text-5xl mb-3">📸</div>
        <h1 class="text-3xl font-bold text-warm-orange" style="font-family: serif;">家庭相册</h1>
        <p class="text-warm-caramel mt-2 text-sm">记录每一个温暖瞬间</p>
      </div>

      <div class="flex mb-6 bg-warm-cream rounded-xl p-1">
        <button @click="isLogin = true"
          :class="isLogin ? 'bg-white shadow-sm text-warm-orange' : 'text-warm-caramel'"
          class="flex-1 py-2 rounded-lg font-medium transition-all duration-200">登录</button>
        <button @click="isLogin = false"
          :class="!isLogin ? 'bg-white shadow-sm text-warm-orange' : 'text-warm-caramel'"
          class="flex-1 py-2 rounded-lg font-medium transition-all duration-200">注册</button>
      </div>

      <form @submit.prevent="submit" class="space-y-4">
        <div>
          <input v-model="form.username" placeholder="用户名"
            class="w-full px-4 py-3 rounded-xl border border-warm-peach bg-warm-cream focus:outline-none focus:ring-2 focus:ring-warm-orange text-warm-caramel" />
        </div>
        <div>
          <input v-model="form.password" type="password" placeholder="密码"
            class="w-full px-4 py-3 rounded-xl border border-warm-peach bg-warm-cream focus:outline-none focus:ring-2 focus:ring-warm-orange text-warm-caramel" />
        </div>
        <div v-if="!isLogin">
          <input v-model="form.nickname" placeholder="昵称（选填）"
            class="w-full px-4 py-3 rounded-xl border border-warm-peach bg-warm-cream focus:outline-none focus:ring-2 focus:ring-warm-orange text-warm-caramel" />
        </div>
        <div v-if="error" class="text-red-400 text-sm text-center bg-red-50 py-2 rounded-lg">{{ error }}</div>
        <button type="submit"
          class="w-full py-3 bg-warm-orange text-white rounded-xl font-medium hover:bg-orange-600 transition-all duration-200 shadow-md hover:shadow-lg transform hover:-translate-y-0.5">
          {{ isLogin ? '✨ 登录' : '🌟 注册' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const isLogin = ref(true)
const error = ref('')
const form = reactive({ username: '', password: '', nickname: '' })

async function submit() {
  error.value = ''
  try {
    if (isLogin.value) await auth.login(form.username, form.password)
    else await auth.register(form.username, form.password, form.nickname)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || '操作失败，请重试'
  }
}
</script>
