<template>
  <div class="publish-page">
    <div class="publish-header">
      <button class="back-btn" @click="$router.back()">←</button>
      <h2>发布动态</h2>
      <button
        class="publish-btn"
        :class="{ active: canPublish }"
        :disabled="publishing || !canPublish"
        @click="submit"
      >
        {{ publishing ? '发布中...' : '发布' }}
      </button>
    </div>

    <div class="publish-content">
      <div class="user-info">
        <img :src="avatar || defaultAvatar" class="user-avatar" />
        <span class="username">{{ username }}</span>
      </div>

      <textarea
        v-model="content"
        class="content-input"
        placeholder="分享这一刻..."
        maxlength="500"
      ></textarea>

      <div class="char-counter" :class="{ warning: content.length > 450 }">
        {{ content.length }} / 500
      </div>

      <div class="image-upload-section">
        <div class="image-upload-header">
          <span>图片</span>
          <span>{{ images.length }}/9</span>
        </div>

        <input
          ref="imageInput"
          type="file"
          accept="image/*"
          multiple
          class="hidden-input"
          @change="handleImageChange"
        />

        <button
          type="button"
          class="image-btn"
          :disabled="uploadingImage || images.length >= 9"
          @click="triggerImagePicker"
        >
          {{ uploadingImage ? '上传中...' : '添加图片' }}
        </button>

        <div v-if="uploadError" class="upload-error">{{ uploadError }}</div>

        <div v-if="images.length" class="image-grid">
          <div v-for="(img, idx) in images" :key="img.id" class="image-cell">
            <img :src="img.url" class="preview-image" />
            <button type="button" class="remove-image" @click.stop="removeImage(idx)">×</button>
          </div>
        </div>
      </div>

      <TagSelector v-model="selectedTags" :max-tags="5" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { postApi, postTagApi } from '../api'
import TagSelector from '../components/TagSelector.vue'

const router = useRouter()
const content = ref('')
const publishing = ref(false)
const uploadingImage = ref(false)
const uploadError = ref('')
const selectedTags = ref([])
const images = ref([])
const imageInput = ref(null)

const username = localStorage.getItem('username') || '用户'
const avatar = localStorage.getItem('avatar') || ''
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const canPublish = computed(() => {
  return content.value.trim().length > 0 || images.value.length > 0
})

const triggerImagePicker = () => {
  imageInput.value?.click()
}

const handleImageChange = async (event) => {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  uploadError.value = ''

  if (!files.length) return

  const remaining = 9 - images.value.length
  if (remaining <= 0) {
    uploadError.value = '最多只能上传 9 张图片'
    return
  }

  const selected = files.slice(0, remaining)
  uploadingImage.value = true

  try {
    for (const file of selected) {
      if (!file.type.startsWith('image/')) {
        throw new Error('仅支持图片文件')
      }
      if (file.size > 10 * 1024 * 1024) {
        throw new Error('图片大小不能超过 10MB')
      }

      const formData = new FormData()
      formData.append('image', file)
      const res = await postApi.uploadImage(formData)
      const imageUrl = res?.data
      if (!imageUrl) {
        throw new Error('上传失败：图片地址为空')
      }
      images.value.push({
        id: `${Date.now()}_${Math.random().toString(36).slice(2, 10)}`,
        url: imageUrl
      })
    }
  } catch (e) {
    uploadError.value = e?.response?.data?.msg || e.message || '上传失败，请重试'
  } finally {
    uploadingImage.value = false
  }
}

const removeImage = (idx) => {
  images.value.splice(idx, 1)
}

const submit = async () => {
  if (!canPublish.value) return

  const userId = localStorage.getItem('userId')
  if (!userId) return router.push('/login')

  publishing.value = true
  try {
    const res = await postApi.add({
      userId: parseInt(userId),
      content: content.value,
      images: images.value.map((item) => item.url)
    })

    const postId = res.data?.postId

    if (selectedTags.value.length > 0 && postId) {
      const tagIds = selectedTags.value.map((t) => t.tagId || t)
      try {
        await postTagApi.add({
          postId,
          userId: parseInt(userId),
          tagIds
        })
      } catch (tagErr) {
        console.error('标签保存失败:', tagErr)
      }
    }

    alert('发布成功')
    router.push('/')
  } catch (e) {
    console.error(e)
    alert('发布失败，请重试')
  } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background: var(--bg);
}

.publish-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: var(--bg);
  cursor: pointer;
}

.publish-header h2 {
  font-size: 18px;
  margin: 0;
}

.publish-btn {
  padding: 8px 18px;
  border: none;
  border-radius: 999px;
  background: #bfbfbf;
  color: #fff;
  cursor: not-allowed;
}

.publish-btn.active {
  background: var(--primary);
  cursor: pointer;
}

.publish-content {
  background: var(--bg-card);
  margin: 20px;
  border-radius: var(--radius);
  padding: 20px;
  box-shadow: var(--shadow);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-weight: 600;
}

.content-input {
  width: 100%;
  min-height: 140px;
  border: none;
  outline: none;
  resize: vertical;
  background: transparent;
  color: var(--text);
}

.char-counter {
  text-align: right;
  font-size: 12px;
  color: var(--text-light);
}

.char-counter.warning {
  color: #ff4d4f;
}

.image-upload-section {
  margin-top: 14px;
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 12px;
}

.image-upload-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.hidden-input {
  display: none;
}

.image-btn {
  margin-top: 8px;
  border: 1px solid var(--border);
  background: var(--bg);
  color: var(--text);
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.image-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.upload-error {
  margin-top: 8px;
  color: #ff4d4f;
  font-size: 12px;
}

.image-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.image-cell {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  background: #f4f4f4;
}

.preview-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  display: block;
}

.remove-image {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  cursor: pointer;
}

@media (max-width: 600px) {
  .publish-content {
    margin: 0;
    border-radius: 0;
  }
}
</style>
