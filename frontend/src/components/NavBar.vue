<template>
  <nav class="bg-white/80 backdrop-blur-sm border-b border-warm-peach sticky top-0 z-50">
    <div class="max-w-5xl mx-auto px-4 h-14 flex items-center justify-between">
      <router-link to="/" class="flex items-center gap-2 text-warm-orange font-bold text-lg" style="font-family: serif;">
        📸 家庭相册
      </router-link>
      <div class="flex items-center gap-4">
        <router-link to="/upload" class="text-warm-caramel hover:text-warm-orange transition-colors text-sm">📤 上传</router-link>
        <router-link to="/people" class="text-warm-caramel hover:text-warm-orange transition-colors text-sm">👨‍👩‍👧 人物</router-link>
        <router-link to="/profile" class="text-warm-caramel hover:text-warm-orange transition-colors text-sm">👤 我的</router-link>
        <span class="text-sm text-warm-caramel hidden sm:inline">{{ auth.user?.nickname }}</span>
        <button @click="logout" class="text-sm text-warm-caramel hover:text-red-400 transition-colors">退出</button>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
const auth = useAuthStore()
const router = useRouter()
function logout() { auth.logout(); router.push('/login') }
</script>
