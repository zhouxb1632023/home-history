<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo">📸</div>
      <h1 class="title">家庭相册</h1>
      <p class="subtitle">记录每一个温暖瞬间</p>

      <div class="tabs">
        <div
          class="tab"
          :class="{ active: isLogin }"
          @click="isLogin = true"
        >
          登录
        </div>
        <div
          class="tab"
          :class="{ active: !isLogin }"
          @click="isLogin = false"
        >
          注册
        </div>
      </div>

      <form @submit.prevent="handleSubmit">
        <div class="field">
          <input
            v-model="username"
            type="text"
            placeholder="用户名"
            autocomplete="username"
            class="input-field"
          />
        </div>
        <div class="field">
          <input
            v-model="password"
            type="password"
            placeholder="密码"
            autocomplete="current-password"
            class="input-field"
          />
        </div>
        <div v-if="!isLogin" class="field">
          <input
            v-model="nickname"
            type="text"
            placeholder="昵称（选填）"
            class="input-field"
          />
        </div>

        <div v-if="error" class="error-msg">{{ error }}</div>

        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '处理中...' : (isLogin ? '✨ 登录' : '🌟 注册') }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const username = ref('')
const password = ref('')
const nickname = ref('')
const error = ref('')
const loading = ref(false)

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    if (isLogin.value) {
      const data = await login(username.value, password.value)
      userStore.setAuth(data)
    } else {
      const data = await register(username.value, password.value, nickname.value)
      userStore.setAuth(data)
    }
    router.replace('/')
  } catch (e) {
    error.value = '操作失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--warm-cream);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 360px;
  background: #fff;
  border-radius: 24px;
  border: 2px solid var(--warm-peach);
  padding: 32px 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.logo {
  text-align: center;
  font-size: 52px;
  line-height: 1;
}

.title {
  text-align: center;
  font-size: 26px;
  font-weight: bold;
  color: var(--warm-orange);
  margin: 10px 0 4px;
}

.subtitle {
  text-align: center;
  color: var(--warm-caramel);
  font-size: 14px;
  margin-bottom: 24px;
}

.tabs {
  display: flex;
  border-radius: 14px;
  overflow: hidden;
  background: var(--warm-cream);
  margin-bottom: 20px;
  height: 46px;
}

.tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.25s;
  -webkit-tap-highlight-color: transparent;
}

.tab.active {
  background: #fff;
  font-weight: 600;
  border-radius: 14px;
}

.field { margin-bottom: 14px; }

.input-field {
  width: 100%;
  padding: 14px 16px;
  border: 1.5px solid var(--warm-peach);
  border-radius: 14px;
  background: var(--warm-cream);
  font-size: 16px;
  outline: none;
  transition: border 0.2s;
  -webkit-appearance: none;
}

.input-field:focus {
  border-color: var(--warm-orange);
  border-width: 1.5px;
  padding: 13px 15px;
}

.input-field::placeholder { color: #bbb; }

.error-msg {
  color: #e53e3e;
  font-size: 13px;
  margin-bottom: 12px;
  text-align: center;
}

.btn-submit {
  width: 100%;
  padding: 16px;
  background: var(--warm-orange);
  color: #fff;
  border: none;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
  margin-top: 6px;
  -webkit-appearance: none;
}

.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
</style>