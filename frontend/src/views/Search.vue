<template>
  <div class="search-page">
    <NavBar />
    
    <main class="main-container">
      <div class="search-header">
        <h1>搜索结果</h1>
        <p class="search-keyword">关键词: "{{ keyword }}"</p>
      </div>
      
      <div class="tabs">
        <span 
          :class="['tab', { active: activeTab === 'user' }]"
          @click="activeTab = 'user'"
        >
          用户
        </span>
        <span 
          :class="['tab', { active: activeTab === 'post' }]"
          @click="activeTab = 'post'"
        >
          帖子
        </span>
      </div>
      
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>搜索中...</p>
      </div>
      
      <div v-else-if="activeTab === 'user'" class="result-list">
        <div 
          v-for="user in users" 
          :key="user.userId"
          class="user-item"
          @click="goUser(user.userId)"
        >
          <img 
            :src="user.avatar || defaultAvatar" 
            class="user-avatar"
            @error="$event.target.src = defaultAvatar"
          />
          <div class="user-info">
            <span class="user-name">@{{ user.username }}</span>
          </div>
        </div>
        
        <div v-if="users.length === 0" class="empty-state">
          <p>未找到相关用户</p>
        </div>
      </div>
      
      <div v-else class="result-list">
        <PostItem 
          v-for="post in posts" 
          :key="post.postId" 
          :post="post"
          @click="goDetail(post.postId)"
        />
        
        <div v-if="posts.length === 0" class="empty-state">
          <p>未找到相关帖子</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { searchApi } from '../api'
import NavBar from '../components/NavBar.vue'
import PostItem from '../components/PostItem.vue'

const router = useRouter()
const route = useRoute()

const keyword = ref('')
const activeTab = ref('user')
const loading = ref(false)
const users = ref([])
const posts = ref([])
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const doSearch = async () => {
  const kw = keyword.value.trim()
  if (!kw) return
  
  loading.value = true
  try {
    if (activeTab.value === 'user') {
      const res = await searchApi.users(kw, 20)
      users.value = res.data || []
    } else {
      const res = await searchApi.posts(kw, 20)
      posts.value = res.data || []
    }
  } catch (e) {
    console.error('搜索失败:', e)
  } finally {
    loading.value = false
  }
}

const goUser = (userId) => {
  router.push(`/profile/${userId}`)
}

const goDetail = (postId) => {
  router.push(`/post/${postId}`)
}

watch(() => route.query.q, (newVal) => {
  if (newVal) {
    keyword.value = newVal
    doSearch()
  }
}, { immediate: true })

watch(activeTab, () => {
  doSearch()
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: var(--bg);
}

.main-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  margin-bottom: 20px;
}

.search-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 8px;
}

.search-keyword {
  color: var(--text-secondary);
  font-size: 14px;
}

.tabs {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--border);
}

.tab {
  padding: 12px 0;
  font-size: 16px;
  color: var(--text-secondary);
  cursor: pointer;
  position: relative;
  transition: color 0.2s ease;
}

.tab:hover {
  color: var(--primary);
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

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-item:hover {
  background: var(--bg);
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--text);
}

.loading-state {
  text-align: center;
  padding: 60px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--bg);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-light);
}
</style>
