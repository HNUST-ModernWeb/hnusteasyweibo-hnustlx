<template>
  <div class="discover-page">
    <NavBar />
    
    <main class="main-container">
      <div class="discover-header">
        <h1>发现</h1>
        <p>发现更多精彩内容</p>
      </div>
      
      <div class="section">
        <h2 class="section-title">热门帖子</h2>
        <div class="post-list">
          <PostItem 
            v-for="post in hotPosts" 
            :key="post.postId" 
            :post="post"
            @click="goDetail(post.postId)"
          />
        </div>
      </div>
      
      <div class="section">
        <h2 class="section-title">热门用户</h2>
        <div class="user-grid">
          <div 
            v-for="user in hotUsers" 
            :key="user.userId"
            class="user-card"
            @click="goUser(user.userId)"
          >
            <img 
              :src="user.avatar || defaultAvatar" 
              class="user-avatar"
              @error="$event.target.src = defaultAvatar"
            />
            <span class="user-name">@{{ user.username }}</span>
            <span class="user-likes">❤️ {{ user.totalLikes || 0 }}</span>
          </div>
        </div>
      </div>
      
      <div class="section">
        <h2 class="section-title">热门标签</h2>
        <div class="tag-cloud">
          <span 
            v-for="tag in hotTags" 
            :key="tag.tagId"
            class="tag-item"
            @click="goTag(tag.tagId)"
          >
            #{{ tag.tagName }}
          </span>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi, tagApi, postApi } from '../api'
import NavBar from '../components/NavBar.vue'
import PostItem from '../components/PostItem.vue'

const router = useRouter()
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const hotPosts = ref([])
const hotUsers = ref([])
const hotTags = ref([])

const fetchHotData = async () => {
  try {
    const postsRes = await postApi.list({ page: 1, pageSize: 10 })
    hotPosts.value = postsRes.data?.records || []
    
    const usersRes = await userApi.getHot(6)
    hotUsers.value = usersRes.data || []
    
    const tagsRes = await tagApi.getHot(15)
    hotTags.value = tagsRes.data || []
  } catch (e) {
    console.error('获取热门数据失败:', e)
  }
}

const goDetail = (postId) => {
  router.push(`/post/${postId}`)
}

const goUser = (userId) => {
  router.push(`/profile/${userId}`)
}

const goTag = (tagId) => {
  router.push({ path: '/', query: { tag: tagId } })
}

onMounted(() => {
  fetchHotData()
})
</script>

<style scoped>
.discover-page {
  min-height: 100vh;
  background: var(--bg);
}

.main-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  padding-bottom: 80px;
}

.discover-header {
  text-align: center;
  padding: 30px 0;
}

.discover-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 8px;
}

.discover-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 3px solid var(--primary);
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.user-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-size: 13px;
  color: var(--text);
  font-weight: 500;
}

.user-likes {
  font-size: 12px;
  color: var(--text-light);
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 8px 16px;
  background: var(--bg-card);
  border-radius: var(--radius-full);
  font-size: 14px;
  color: var(--primary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag-item:hover {
  background: rgba(24, 144, 255, 0.1);
}

@media (max-width: 600px) {
  .user-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
