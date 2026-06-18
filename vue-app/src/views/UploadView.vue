<template>
  <div class="upload-page">
    <!-- 顶部导航 -->
    <nav class="navbar">
      <button class="back-btn" @click="$router.push('/')">←</button>
      <span class="nav-title">📤 上传照片</span>
    </nav>

    <!-- 内容区 -->
    <div class="upload-body">
      <div class="pick-area" @click="pickFiles">
        <div class="pick-icon">📁</div>
        <p>点击选择照片或视频</p>
        <span class="pick-hint">支持多选</span>
      </div>

      <div v-if="files.length > 0" class="file-list">
        <p class="file-count">已选择 {{ files.length }} 个文件</p>
        <div v-for="(f, i) in files" :key="i" class="file-item">
          <span class="file-name">{{ f.name }}</span>
          <button class="file-remove" @click="removeFile(i)">✕</button>
        </div>
      </div>

      <textarea
        v-model="description"
        rows="3"
        placeholder="记录这一刻的故事..."
        class="desc-input"
      ></textarea>

      <div v-if="tags.length > 0" class="tag-section">
        <p class="section-label">🏷️ 选择标签</p>
        <div class="tag-list">
          <span
            v-for="tag in tags"
            :key="tag.id"
            class="tag-chip"
            :class="{ active: selectedTags.includes(tag.id) }"
            :style="{ backgroundColor: tag.color || '#E07B4C' }"
            @click="toggleTag(tag)"
          >
            {{ tag.name }}
          </span>
        </div>
      </div>

      <!-- 拍摄日期 -->
      <div class="date-section">
        <div class="date-row">
          <label class="date-label">📅 拍摄日期</label>
          <input
            type="date"
            v-model="takenAt"
            class="filter-input taken-at-input"
          />
        </div>
      </div>

      <div v-if="result" class="result-msg" :class="resultType">
        {{ result }}
      </div>

      <button
        class="btn-upload"
        :disabled="files.length === 0 || uploading"
        @click="doUpload"
      >
        {{ uploading ? '上传中...' : '🌟 开始上传' }}
      </button>
    </div>

    <!-- 底部导航栏 -->
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getTags, uploadMedia } from '@/api'

const router = useRouter()
const takenAt = ref('')
const files = ref([])
const description = ref('')
const tags = ref([])
const selectedTags = ref([])
const uploading = ref(false)
const result = ref('')
const resultType = ref('')

function todayStr() {
  const now = new Date()
  return now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0') + '-' + String(now.getDate()).padStart(2, '0')
}

function onFileSelect(e) {
  const list = Array.from(e.target.files || [])
  files.value = [...files.value, ...list]
  e.target.value = ''
}

function pickFiles() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*,video/*'
  input.multiple = true
  input.addEventListener('change', onFileSelect)
  input.click()
}

function removeFile(index) {
  files.value.splice(index, 1)
}

function toggleTag(tag) {
  const idx = selectedTags.value.indexOf(tag.id)
  if (idx >= 0) selectedTags.value.splice(idx, 1)
  else selectedTags.value.push(tag.id)
}

async function doUpload() {
  if (files.value.length === 0) return
  uploading.value = true
  result.value = ''
  try {
    const formData = new FormData()
    files.value.forEach((f) => formData.append('files', f))
    if (description.value.trim()) formData.append('description', description.value.trim())
    selectedTags.value.forEach((id) => formData.append('tagIds', String(id)))
    formData.append('takenAt', takenAt.value || todayStr())
    const data = await uploadMedia(formData)
    result.value = `成功上传 ${data.length} 个文件！`
    resultType.value = 'success'
    files.value = []
    description.value = ''
    selectedTags.value = []
    takenAt.value = todayStr()
    setTimeout(() => { router.replace('/') }, 1500)
  } catch (e) {
    result.value = '上传失败'
    resultType.value = 'error'
  } finally {
    uploading.value = false
  }
}

async function initPage() {
  takenAt.value = todayStr()
  try { tags.value = await getTags() } catch (e) { /* ignore */ }
  // 等 DOM 渲染后手动设置 input 值（Vue v-model 对 type=date 有时不直接生效）
  await nextTick()
  const el = document.querySelector('.taken-at-input')
  if (el) el.value = takenAt.value
}

onMounted(initPage)
</script>

<style scoped>
.upload-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: var(--warm-cream);
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
.navbar {
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid var(--warm-peach);
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.back-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 4px 8px;
  color: var(--warm-orange);
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 600;
  color: var(--warm-caramel);
}

/* 内容区 */
.upload-body {
  flex: 1;
  padding: 16px;
  padding-bottom: 20px;
  overflow-y: auto;
}

.pick-area {
  border: 2px dashed var(--warm-peach);
  border-radius: 16px;
  padding: 36px 20px;
  text-align: center;
  cursor: pointer;
}

.pick-area:active { border-color: var(--warm-orange); }
.pick-icon { font-size: 44px; margin-bottom: 8px; }
.pick-area p { color: var(--warm-caramel); font-size: 15px; margin-bottom: 4px; }
.pick-hint { font-size: 12px; color: #bbb; }

.file-list { margin-top: 14px; }
.file-count { color: var(--warm-caramel); font-size: 13px; margin-bottom: 8px; }

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #fff;
  border-radius: 10px;
  margin-bottom: 4px;
}

.file-name {
  font-size: 13px;
  color: var(--warm-caramel);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 8px;
}

.file-remove {
  background: none;
  border: none;
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 4px 8px;
}

.desc-input {
  width: 100%;
  margin-top: 14px;
  padding: 12px 14px;
  border: 1.5px solid var(--warm-peach);
  border-radius: 14px;
  background: var(--warm-cream);
  font-size: 15px;
  resize: none;
  outline: none;
  font-family: inherit;
  min-height: 80px;
}

.desc-input:focus { border-color: var(--warm-orange); }

.tag-section { margin-top: 16px; }
.section-label { color: var(--warm-caramel); font-size: 13px; margin-bottom: 8px; }

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-chip {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  background: var(--warm-cream);
  color: var(--warm-caramel);
  border: 1px solid var(--warm-peach);
  cursor: pointer;
  transition: all 0.2s;
  min-height: 36px;
  display: flex;
  align-items: center;
}

.tag-chip.active { color: #fff; }

/* 拍摄日期 */
.date-section {
  margin-top: 16px;
}

.date-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.date-label {
  font-size: 14px;
  color: var(--warm-caramel);
  white-space: nowrap;
}

.filter-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid var(--warm-peach);
  border-radius: 12px;
  background: #fff;
  font-size: 14px;
  color: var(--warm-caramel);
  outline: none;
  min-height: 40px;
}

.filter-input:focus {
  border-color: var(--warm-orange);
  box-shadow: 0 0 0 2px rgba(224, 123, 76, 0.12);
}

.result-msg {
  text-align: center;
  padding: 12px;
  margin: 14px 0;
  border-radius: 12px;
  font-size: 14px;
}

.result-msg.success { background: #f0fff4; color: #38a169; }
.result-msg.error { background: #fff5f5; color: #e53e3e; }

.btn-upload {
  width: 100%;
  margin-top: 24px;
  padding: 16px;
  background: var(--warm-orange);
  color: #fff;
  border: none;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  min-height: 48px;
}

.btn-upload:disabled { opacity: 0.6; cursor: not-allowed; }

</style>