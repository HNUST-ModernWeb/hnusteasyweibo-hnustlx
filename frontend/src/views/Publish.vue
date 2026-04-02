<template>
  <div class="publish-page">
    <div class="publish-header">
      <button class="back-btn" @click="$router.back()">
        <span>←</span>
      </button>
      <h2>发布动态</h2>
      <button 
        class="publish-btn" 
        :class="{ active: canPublish }"
        :disabled="publishing"
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
        placeholder="分享新鲜事... (ง •̀_•́)ง"
        maxlength="500"
        @input="autoResize"
      ></textarea>
      
      <div class="char-counter" :class="{ warning: content.length > 450 }">
        {{ content.length }} / 500
      </div>
      
      <TagSelector v-model="selectedTags" :max-tags="5" />
    </div>
    
    <div v-if="showEmoji" class="emoji-panel">
      <div class="emoji-grid">
        <span v-for="emoji in emojis" :key="emoji" class="emoji" @click="insertEmoji(emoji)">
          {{ emoji }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { postApi, postTagApi } from '../api'
import TagSelector from '../components/TagSelector.vue'

const router = useRouter()
const content = ref('')
const publishing = ref(false)
const showEmoji = ref(false)
const selectedTags = ref([])
const username = localStorage.getItem('username') || '用户'
const avatar = localStorage.getItem('avatar') || ''
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const emojis = ['😀', '😂', '😊', '😍', '🤔', '😢', '😮', '👍', '👎', '❤️', '🎉', '🔥', '💪', '✨', '🌈', '🍀', '🍕', '🚗', '✈️', '📸']

const canPublish = computed(() => {
  return content.value.trim().length > 0
})

const autoResize = (e) => {
  e.target.style.height = 'auto'
  e.target.style.height = e.target.scrollHeight + 'px'
}

const insertEmoji = (emoji) => {
  content.value += emoji
  showEmoji.value = false
}

const submit = async () => {
  if (!canPublish.value) return alert('请输入内容或添加图片')
  
  const userId = localStorage.getItem('userId')
  if (!userId) return router.push('/login')
  
  publishing.value = true
  try {
    const postData = {
      userId: parseInt(userId),
      content: content.value
    }
    
    const res = await postApi.add(postData)
    const postId = res.data?.postId
    
    if (selectedTags.value.length > 0 && postId) {
      const tagIds = selectedTags.value.map(t => t.tagId || t)
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
    
    alert('发布成功！')
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
  width: 40px;
  height: 40px;
  border: none;
  background: var(--bg);
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
}

.back-btn:hover {
  background: #e0e0e0;
}

.publish-header h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
}

.publish-btn {
  padding: 10px 24px;
  border: none;
  border-radius: var(--radius-full);
  background: #ccc;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: not-allowed;
  transition: var(--transition);
}

.publish-btn.active {
  background: var(--primary);
  cursor: pointer;
}

.publish-btn.active:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(24, 144, 255, 0.4);
}

.publish-btn:disabled {
  opacity: 0.7;
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
  gap: 12px;
  margin-bottom: 16px;
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}

.content-input {
  width: 100%;
  min-height: 150px;
  border: none;
  resize: none;
  font-size: 16px;
  line-height: 1.6;
  color: var(--text);
  outline: none;
  font-family: inherit;
}

.content-input::placeholder {
  color: #aaa;
}

.char-counter {
  text-align: right;
  font-size: 12px;
  color: var(--text-light);
  margin-top: 8px;
}

.char-counter.warning {
  color: #ff6b6b;
}

.emoji-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--bg-card);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  padding: 20px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 10px;
}

.emoji {
  font-size: 24px;
  text-align: center;
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-sm);
  transition: all 0.2s ease;
}

.emoji:hover {
  background: var(--bg);
  transform: scale(1.2);
}

@media (max-width: 600px) {
  .publish-content {
    margin: 0;
    border-radius: 0;
  }
}
</style>