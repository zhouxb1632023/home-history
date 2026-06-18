<template>
  <div class="min-h-screen bg-warm-cream">
    <NavBar />
    <main class="max-w-2xl mx-auto px-4 py-8">
      <h2 class="text-2xl font-bold text-warm-orange mb-6 text-center" style="font-family: serif;">📷 上传照片/视频</h2>

      <div
        class="border-2 border-dashed rounded-3xl p-10 text-center mb-6 transition-all duration-200"
        :class="isDragging ? 'border-warm-orange bg-warm-yellow/30' : 'border-warm-peach bg-white'"
        @dragover.prevent="isDragging = true"
        @dragleave="isDragging = false"
        @drop.prevent="onDrop"
      >
        <div class="text-5xl mb-3">📧</div>
        <p class="text-warm-caramel mb-2">拖拽文件到这里，或点击选择</p>
        <p class="text-xs text-gray-400">支持 JPG、PNG、HEIC、MP4、MOV · 原文件不压缩</p>
        <input ref="fileInput" type="file" multiple accept="image/*,video/*" @change="onFileChange" class="hidden" />
        <button @click="$refs.fileInput.click()" class="mt-4 px-6 py-2 bg-warm-orange text-white rounded-xl hover:bg-orange-600 transition-colors">选择文件</button>
      </div>

      <div v-if="files.length" class="bg-white rounded-2xl p-4 border border-warm-peach mb-6">
        <h3 class="text-sm font-medium text-warm-caramel mb-3">已选择 {{ files.length }} 个文件</h3>
        <div class="max-h-40 overflow-y-auto space-y-2">
          <div v-for="(f, i) in files" :key="i" class="flex items-center gap-3 text-sm text-warm-caramel">
            <span class="truncate flex-1">{{ f.name }}</span>
            <span class="text-gray-400 text-xs whitespace-nowrap">({{ formatSize(f.size) }})</span>
            <button @click="removeFile(i)" class="text-red-400 hover:text-red-600">✗</button>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-5 border border-warm-peach mb-6 space-y-4">
        <div>
          <label class="block text-sm font-medium text-warm-caramel mb-2">📑 文字说明</label>
          <textarea v-model="description" rows="3" placeholder="记录这一刻的故事..."
            class="w-full px-4 py-3 rounded-xl border border-warm-peach bg-warm-cream focus:outline-none focus:ring-2 focus:ring-warm-orange text-warm-caramel resize-none"></textarea>
        </div>
        <div>
          <label class="block text-sm font-medium text-warm-caramel mb-2">🏷 人物标签</label>
          <div v-if="tagsError" class="text-red-400 text-sm">人物标签加载失败，请刷新页面重试</div>
          <div v-else-if="tags.length === 0" class="text-gray-400 text-sm">暂无标签，请在「人物」页面创建</div>
          <div v-else class="flex flex-wrap gap-2">
            <button v-for="tag in tags" :key="tag.id" @click="toggleTag(tag.id)"
              :class="selectedTags.includes(tag.id) ? 'ring-2 ring-offset-1 opacity-100' : 'opacity-60'"
              class="px-3 py-1.5 rounded-full text-white text-sm transition-all duration-200"
              :style="{ backgroundColor: tag.color }">{{ tag.name }}</button>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-warm-caramel mb-2">📮 拍摄日期</label>
          <input type="date" v-model="takenAt"
            class="px-4 py-3 rounded-xl border border-warm-peach bg-warm-cream focus:outline-none focus:ring-2 focus:ring-warm-orange text-warm-caramel" />
        </div>
      </div>

      <div v-if="uploading" class="bg-white rounded-2xl p-4 border border-warm-peach mb-6 text-center">
        <div class="text-warm-orange animate-pulse">正在上传中...</div>
      </div>
      <div v-if="uploadResult" class="bg-green-50 rounded-2xl p-4 border border-green-200 mb-6 text-center text-green-600">
        ✓ 成功上传 {{ uploadResult }} 个文件！
      </div>

      <button @click="doUpload" :disabled="files.length === 0 || uploading"
        class="w-full py-3 bg-warm-orange text-white rounded-xl font-medium hover:bg-orange-600 transition-all duration-200 disabled:opacity-50 shadow-md">
        {{ uploading ? '上传中...' : '🚀 开始上传' }}
      </button>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../utils/api'
import NavBar from '../components/NavBar.vue'

const router = useRouter()
const files = ref([])
const tags = ref([])
const tagsError = ref(false)
const description = ref('')
const selectedTags = ref([])
const takenAt = ref('')
const isDragging = ref(false)
const uploading = ref(false)
const uploadResult = ref(null)

function formatSize(bytes) {
  if (!bytes) return '0 KB'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
function onDrop(e) { isDragging.value = false; addFiles(e.dataTransfer.files) }
function onFileChange(e) { addFiles(e.target.files) }
function addFiles(list) { files.value.push(...Array.from(list)) }
function removeFile(i) { files.value.splice(i, 1) }
function toggleTag(id) {
  const idx = selectedTags.value.indexOf(id)
  if (idx >= 0) selectedTags.value.splice(idx, 1)
  else selectedTags.value.push(id)
}

async function doUpload() {
  uploading.value = true; uploadResult.value = null
  try {
    const formData = new FormData()
    files.value.forEach(f => formData.append('files', f))
    if (description.value) formData.append('description', description.value)
    if (selectedTags.value.length > 0) selectedTags.value.forEach(id => formData.append('tagIds', id))
    if (takenAt.value) formData.append('takenAt', takenAt.value + 'T00:00:00')
    const { data } = await api.post('/media/upload', formData)
    uploadResult.value = data.length
    files.value = []; description.value = ''; selectedTags.value = []
    setTimeout(() => router.push('/'), 1500)
  } catch (e) {
    alert('上传失败: ' + (e.response?.data?.message || e.message))
  } finally { uploading.value = false }
}

onMounted(async () => {
  try {
    const { data } = await api.get('/tags')
    tags.value = data
  } catch (e) {
    tagsError.value = true
    console.error('加载人物标签失败', e)
  }
})
</script>