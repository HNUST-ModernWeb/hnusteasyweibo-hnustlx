<template>
  <div class="messages-page">
    <NavBar />
    
    <main class="main-container">
      <div class="messages-header">
        <h1>消息</h1>
      </div>
      
      <div class="tabs">
        <span 
          :class="['tab', { active: activeTab === 'like' }]"
          @click="switchTab('like')"
        >
          点赞
        </span>
        <span 
          :class="['tab', { active: activeTab === 'comment' }]"
          @click="switchTab('comment')"
        >
          评论
        </span>
      </div>
      
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
      </div>
      
      <div v-else-if="activeTab === 'like'" class="message-list">
        <div 
          v-for="like in likes" 
          :key="like.likeId"
          class="message-item"
          @click="goDetail(like.postId)"
        >
          <img :src="like.avatar || defaultAvatar" class="message-avatar" />
          <div class="message-content">
            <p class="message-text">
              <span class="user-name">{{ like.username || '某人' }}</span> 赞了你的帖子
            </p>
            <p class="message-preview">{{ like.postContent }}</p>
            <span class="message-time">{{ formatTime(like.createTime) }}</span>
          </div>
        </div>
        
        <div v-if="likes.length === 0" class="empty-state">
          <span class="empty-icon">❤️</span>
          <p>暂无点赞消息</p>
        </div>
      </div>
      
      <div v-else class="message-list">
        <div 
          v-for="comment in comments" 
          :key="comment.commentId"
          class="message-item"
          @click="goDetail(comment.postId)"
        >
          <img :src="comment.avatar || defaultAvatar" class="message-avatar" />
          <div class="message-content">
            <p class="message-text">
              <span class="user-name">{{ comment.username || '某人' }}</span> 评论了你的帖子
            </p>
            <p class="message-preview">{{ comment.postContent }}</p>
            <span class="message-time">{{ formatTime(comment.createTime) }}</span>
          </div>
        </div>
        
        <div v-if="comments.length === 0" class="empty-state">
          <span class="empty-icon">💬</span>
          <p>暂无评论消息</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { likeApi, commentApi } from '../api'
import NavBar from '../components/NavBar.vue'

const router = useRouter()
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const activeTab = ref('like')
const loading = ref(false)
const likes = ref([])
const comments = ref([])

const fetchLikes = async () => {
  loading.value = true
  try {
    const res = await likeApi.received({ page: 1, pageSize: 20 })
    likes.value = res.data?.records || []
  } catch (e) {
    console.error('获取点赞列表失败:', e)
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await commentApi.received({ page: 1, pageSize: 20 })
    comments.value = res.data?.records || []
  } catch (e) {
    console.error('获取评论列表失败:', e)
  } finally {
    loading.value = false
  }
}

const goDetail = (postId) => {
  router.push(`/post/${postId}`)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return time.split('T')[0]
}

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'like') {
    fetchLikes()
  } else {
    fetchComments()
  }
}

onMounted(() => {
  fetchLikes()
})
</script>

<style scoped>
.messages-page {
  min-height: 100vh;
  background: var(--bg);
}

.main-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  padding-bottom: 80px;
}

.messages-header {
  margin-bottom: 20px;
}

.messages-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text);
}

.tabs {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--border);
}

.tab {
  padding: 12px 0;
  font-size: 15px;
  color: var(--text-secondary);
  cursor: pointer;
  position: relative;
}

.tab.active {
  color: var(--primary);
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--primary);
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.message-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--radius);
  cursor: pointer;
  transition: background 0.2s ease;
}

.message-item:hover {
  background: var(--bg);
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-text {
  font-size: 14px;
  color: var(--text);
  margin-bottom: 4px;
}

.user-name {
  font-weight: 500;
  color: var(--primary);
}

.message-preview {
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.message-time {
  font-size: 12px;
  color: var(--text-light);
}

.loading-state {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid var(--bg);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.empty-state p {
  color: var(--text-light);
}
</style>
