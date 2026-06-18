<template>
  <div class="min-h-screen bg-warm-cream">
    <NavBar />
    <main class="max-w-xl mx-auto px-4 py-8">
      <div class="bg-white rounded-3xl shadow-lg border-2 border-dashed border-warm-peach overflow-hidden">
        <!-- Header -->
        <div class="bg-gradient-to-r from-warm-orange to-orange-400 px-6 pt-8 pb-6 text-center">
          <h1 class="text-2xl font-bold text-white" style="font-family: serif;">我的</h1>
        </div>

        <!-- Avatar & Info -->
        <div class="px-6 pb-6">
          <!-- Avatar -->
          <div class="flex flex-col items-center -mt-12 mb-6">
            <div
              @click="triggerAvatarUpload"
              class="relative w-24 h-24 rounded-full border-4 border-white shadow-lg cursor-pointer overflow-hidden hover:opacity-80 transition-opacity bg-warm-yellow"
            >
              <img
                v-if="auth.avatarUrl"
                :src="avatarSrc"
                alt="头像"
                class="w-full h-full object-cover"
              />
              <div v-else class="w-full h-full flex items-center justify-center text-4xl">
                {{ defaultAvatarEmoji }}
              </div>
              <div class="absolute inset-0 bg-black/30 flex items-center justify-center opacity-0 hover:opacity-100 transition-opacity rounded-full">
                <span class="text-white text-xs">更换</span>
              </div>
            </div>
            <input
              ref="avatarInput"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleAvatarUpload"
            />
            <p class="text-warm-caramel text-sm mt-2">点击头像修改</p>
          </div>

          <!-- Username -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-warm-caramel mb-1">用户名</label>
            <div class="px-4 py-3 bg-warm-cream rounded-xl text-warm-caramel text-sm">
              {{ auth.user?.username }}
            </div>
          </div>

          <!-- Nickname -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-warm-caramel mb-1">昵称</label>
            <div class="flex gap-2">
              <input
                v-model="nickname"
                placeholder="输入昵称"
                class="flex-1 px-4 py-3 rounded-xl border border-warm-peach bg-warm-cream focus:outline-none focus:ring-2 focus:ring-warm-orange text-warm-caramel text-sm"
              />
              <button
                @click="saveNickname"
                :disabled="saving"
                class="px-4 py-3 bg-warm-orange text-white rounded-xl text-sm hover:bg-orange-600 transition-colors disabled:opacity-50"
              >
                {{ saving ? '保存中...' : '保存' }}
              </button>
            </div>
          </div>

          <!-- Divider -->
          <div class="border-t border-warm-peach my-6"></div>

          <!-- Account Switch / Logout -->
          <div class="space-y-3">
            <button
              @click="switchAccount"
              class="w-full py-3 bg-white border-2 border-warm-orange text-warm-orange rounded-xl font-medium hover:bg-warm-yellow/30 transition-all text-sm"
            >
              👤 切换账号
            </button>
            <button
              @click="handleLogout"
              class="w-full py-3 bg-white border-2 border-red-300 text-red-400 rounded-xl font-medium hover:bg-red-50 transition-all text-sm"
            >
              🚪 退出登录
            </button>
          </div>
        </div>
      </div>

      <!-- Upload progress -->
      <div v-if="uploading" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
        <div class="bg-white rounded-2xl px-8 py-6 text-center">
          <div class="text-3xl mb-2">⏳</div>
          <p class="text-warm-caramel text-sm">上传中...</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import NavBar from '../components/NavBar.vue'

const auth = useAuthStore()
const router = useRouter()
const nickname = ref(auth.user?.nickname || '')
const saving = ref(false)
const uploading = ref(false)
const avatarInput = ref(null)

// Cartoon avatars for new users
const cartoonAvatars = ['🐱', '🐶', '🦊', '🐼', '🐨', '🐯', '🦁', '🐰', '🐻', '🐸']
const defaultAvatarEmoji = cartoonAvatars[Math.floor(Math.random() * cartoonAvatars.length)]

// Avatar image source - use backend endpoint if user has avatarUrl
const avatarSrc = computed(() => {
  if (auth.avatarUrl) {
    return '/api/auth/avatar?t=' + Date.now()
  }
  return ''
})

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

async function handleAvatarUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    await auth.uploadAvatar(file)
    nickname.value = auth.user?.nickname || ''
  } catch (e) {
    console.error('头像上传失败', e)
    alert('头像上传失败，请重试')
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

async function saveNickname() {
  if (!nickname.value.trim()) return
  saving.value = true
  try {
    await auth.updateProfile(nickname.value.trim())
  } catch (e) {
    console.error('保存昵称失败', e)
    alert('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

function switchAccount() {
  auth.logout()
  router.push('/login')
}

function handleLogout() {
  if (confirm('确定要退出登录吗？')) {
    auth.logout()
    router.push('/login')
  }
}

onMounted(() => {
  nickname.value = auth.user?.nickname || ''
})
</script>
