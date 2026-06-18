<template>
  <div id="app">
    <router-view />
    <!-- 底部导航栏（登录页隐藏） -->
    <nav v-if="showBottomNav" class="bottom-nav">
      <router-link to="/" class="nav-item" active-class="active" :exact="true">
        <span class="nav-icon">🏠</span>
        <span class="nav-label">相册</span>
      </router-link>
      <router-link to="/upload" class="nav-item" active-class="active">
        <span class="nav-icon">📤</span>
        <span class="nav-label">上传</span>
      </router-link>
      <router-link to="/profile" class="nav-item" active-class="active">
        <span class="nav-icon">👤</span>
        <span class="nav-label">我的</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const showBottomNav = computed(() => route.name !== 'Login')
</script>

<style>
#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>

<style>
/* 共享底部导航栏 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-top: 1px solid var(--warm-peach);
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--warm-caramel);
  font-size: 11px;
  padding: 4px 16px;
  gap: 2px;
  min-height: 48px;
  text-decoration: none;
}

.nav-icon { font-size: 22px; line-height: 1; }
.nav-item.active { color: var(--warm-orange); }
</style>