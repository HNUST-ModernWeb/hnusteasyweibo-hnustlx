<template>
  <div class="home">
    <NavBar />
    
    <main class="main-container">
      <div class="content-wrapper">
        <div class="feed-column">
          <div class="feed-header">
            <h1>动态广场</h1>
            <div class="header-actions">
              <div class="refresh-btn" @click="refresh">
                <span :class="{ spinning: loading }">🔄</span>
                <span>刷新</span>
              </div>
              <div class="tag-selector">
                <button class="tag-selector-btn" @click="toggleTagDropdown">
                  <span>🏷️</span>
                  <span>选择标签</span>
                  <span class="arrow">{{ showTagDropdown ? '▲' : '▼' }}</span>
                </button>
                <div v-if="showTagDropdown" class="tag-dropdown" ref="tagDropdownRef">
                  <div class="tag-dropdown-header">
                    <span>选择标签筛选</span>
                    <span class="close-btn" @click="showTagDropdown = false">✕</span>
                  </div>
                  <div class="tag-dropdown-list">
                    <div 
                      :class="['tag-dropdown-item', { active: selectedTagId === null }]"
                      @click="selectTag(null)"
                    >
                      <span class="radio-icon">●</span>
                      <span>全部</span>
                    </div>
                    <div 
                      v-for="tag in allTags" 
                      :key="tag.tagId"
                      :class="['tag-dropdown-item', { active: selectedTagId === tag.tagId }]"
                      @click="selectTag(tag.tagId)"
                    >
                      <span class="radio-icon">○</span>
                      <span>#{{ tag.tagName }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="tag-filter-bar">
            <span 
              :class="['filter-tag', { active: selectedTagId === null }]"
              @click="selectTag(null)"
            >
              全部
            </span>
            <span 
              v-for="tag in displayTags" 
              :key="tag.tagId"
              :class="['filter-tag', { active: selectedTagId === tag.tagId }]"
              @click="selectTag(tag.tagId)"
            >
              #{{ tag.tagName }}
            </span>
            <span 
              v-if="hotTags.length > maxDisplayTags" 
              class="filter-tag more-btn"
              @click="showMoreTags = !showMoreTags"
            >
              {{ showMoreTags ? '收起' : `更多(${hotTags.length - maxDisplayTags})` }}
            </span>
          </div>
          
          <div class="post-list">
            <PostItem 
              v-for="post in posts" 
              :key="post.postId" 
              :post="post" 
              @click="goDetail(post.postId)"
              @refresh="fetchPosts"
              @like-changed="refreshHotUsers"
            />
            
            
            <div v-if="!loading && posts.length === 0" class="empty-state">
              <div class="empty-icon">📝</div>
              <p>还没有任何动态</p>
              <p class="sub-text">快去发布第一条动态吧</p>
            </div>
            
            <div v-if="!hasMore && posts.length > 0" class="end-tip">
              <span>— 已经到底啦 —</span>
            </div>
            
            <div v-if="totalRecords > 0" class="pagination">
              <button 
                class="page-btn" 
                :disabled="page <= 1"
                @click="changePage(-1)"
              >
                上一页
              </button>
              <span class="page-info">第 {{ page }} / {{ totalPages }} 页</span>
              <button 
                class="page-btn" 
                :disabled="page >= totalPages"
                @click="changePage(1)"
              >
                下一页
              </button>
            </div>
          </div>
        </div>
        
        <aside class="sidebar">
          <div class="sidebar-section">
            <h3 class="section-title">热门标签</h3>
            <div class="tag-list">
              <span 
                v-for="tag in displaySidebarTags" 
                :key="tag.tagId" 
                class="tag-item"
                @click="selectTag(tag.tagId)"
              >
                #{{ tag.tagName }}
              </span>
            </div>
            <div 
              v-if="hotTags.length > 10" 
              class="show-more"
              @click="showAllSidebarTags = !showAllSidebarTags"
            >
              {{ showAllSidebarTags ? '收起' : `展开更多 (${hotTags.length - 10})` }}
            </div>
          </div>
          
          <div v-if="hotTags.length === 0" class="sidebar-section">
            <div class="empty-tip">暂无标签</div>
          </div>
          
          <div v-if="hotUsers.length > 0" class="sidebar-section">
            <h3 class="section-title">热门博主</h3>
            <div class="hot-user-list">
              <div 
                v-for="(user, index) in hotUsers" 
                :key="user.userId"
                class="hot-user-item"
                @click="goUser(user.userId)"
              >
                <span class="user-rank" :class="'rank-' + (index + 1)">
                  {{ index + 1 }}
                </span>
                <img 
                  :src="user.avatar || '/upload/avatar/default.jpg'" 
                  class="user-avatar"
                  @error="$event.target.src = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'"
                />
                <div class="user-info">
                  <span class="user-name">@{{ user.username }}</span>
                  <span class="user-likes">❤️ {{ user.totalLikes || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { postApi, tagApi, postTagApi, userApi } from '../api'
import NavBar from '../components/NavBar.vue'
import PostItem from '../components/PostItem.vue'

const router = useRouter()
const posts = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(5)
const totalRecords = ref(0)
const hasMore = ref(true)
const hotTags = ref([])
const selectedTagId = ref(null)
const showMoreTags = ref(false)
const showAllSidebarTags = ref(false)
const showTagDropdown = ref(false)
const allTags = ref([])
const tagDropdownRef = ref(null)
const maxDisplayTags = 9
const hotUsers = ref([])

const displayTags = computed(() => {
  if (showMoreTags.value) {
    return hotTags.value
  }
  return hotTags.value.slice(0, maxDisplayTags)
})

const displaySidebarTags = computed(() => {
  if (showAllSidebarTags.value) {
    return hotTags.value
  }
  return hotTags.value.slice(0, 9)
})

const totalPages = computed(() => {
  return Math.ceil(totalRecords.value / pageSize.value) || 1
})

const fetchPosts = async (reset = false) => {
  if (loading.value) return
  
  if (reset) {
    page.value = 1
    posts.value = []
  }
  
  loading.value = true
  try {
    const params = { page: page.value, pageSize: pageSize.value }
    const res = await postApi.list(params)
    totalRecords.value = res.data.total || 0
    let newPosts = res.data.records || []
    
    if (selectedTagId.value !== null) {
      const filteredPosts = []
      for (const post of newPosts) {
        try {
          const tagsRes = await postTagApi.getList(post.postId)
          const tags = tagsRes.data || []
          if (tags.some(t => t.tagId === selectedTagId.value)) {
            filteredPosts.push(post)
          }
        } catch (e) {
          console.error('获取帖子标签失败:', e)
        }
      }
      newPosts = filteredPosts
    }
    
    posts.value = newPosts
    hasMore.value = page.value < totalPages.value
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const changePage = (delta) => {
  const newPage = page.value + delta
  if (newPage >= 1 && newPage <= totalPages.value) {
    page.value = newPage
    fetchPosts()
  }
}

const selectTag = (tagId) => {
  selectedTagId.value = tagId
  showMoreTags.value = false
  showTagDropdown.value = false
  fetchPosts(true)
}

const toggleTagDropdown = async () => {
  showTagDropdown.value = !showTagDropdown.value
  if (showTagDropdown.value && allTags.value.length === 0) {
    await fetchAllTags()
  }
}

const fetchAllTags = async () => {
  try {
    const res = await tagApi.list({ page: 1, pageSize: 100 })
    allTags.value = res.data?.records || []
  } catch (e) {
    console.error('获取所有标签失败:', e)
  }
}

const closeTagDropdown = (e) => {
  const selectorBtn = document.querySelector('.tag-selector-btn')
  if (selectorBtn && selectorBtn.contains(e.target)) {
    return
  }
  if (tagDropdownRef.value && !tagDropdownRef.value.contains(e.target)) {
    showTagDropdown.value = false
  }
}

const goTag = (tagId) => {
  selectTag(tagId)
}

const fetchSidebarData = async () => {
  try {
    const tagsRes = await tagApi.getHot(9)
    hotTags.value = tagsRes.data || []
    
    const usersRes = await userApi.getHot(5)
    hotUsers.value = usersRes.data || []
  } catch (e) {
    console.error('获取侧边数据失败:', e)
  }
}

const goUser = (userId) => {
  router.push(`/profile/${userId}`)
}

const goDetail = (postId) => {
  router.push(`/post/${postId}`)
}

const refresh = () => {
  fetchPosts(true)
}

const refreshHotUsers = async () => {
  try {
    const usersRes = await userApi.getHot(5)
    hotUsers.value = usersRes.data || []
  } catch (e) {
    console.error('刷新热门博主失败:', e)
  }
}

const handleScroll = () => {
  const scrollBottom = window.innerHeight + window.scrollY
  const docHeight = document.documentElement.scrollHeight
  
  if (scrollBottom >= docHeight - 200) {
    fetchPosts()
  }
}

onMounted(() => {
  fetchPosts(true)
  fetchSidebarData()
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', closeTagDropdown)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', closeTagDropdown)
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: var(--bottom-nav-height);
}

.main-container {
  max-width: 1300px;
  margin: 0 auto;
  padding: 20px;
}

.content-wrapper {
  display: flex;
  gap: 24px;
  justify-content: center;
}

.feed-column {
  flex: 1;
  max-width: 540px;
  min-width: 0;
}

.feed-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 8px;
}

.feed-header h1 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text);
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tag-selector {
  position: relative;
}

.tag-selector-btn {
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--bg-card);
  border: 1px solid #e0e0e0;
  border-radius: var(--radius-full);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
  transition: var(--transition);
}

.tag-selector-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.tag-selector-btn .arrow {
  font-size: 10px;
}

.tag-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 220px;
  max-height: 320px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;
  overflow: hidden;
  animation: dropdownFade 0.2s ease;
}

@keyframes dropdownFade {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tag-dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}

.tag-dropdown-header .close-btn {
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 12px;
}

.tag-dropdown-header .close-btn:hover {
  color: var(--text);
}

.tag-dropdown-list {
  max-height: 260px;
  overflow-y: auto;
  padding: 8px 0;
}

.tag-dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
  transition: all 0.2s ease;
}

.tag-dropdown-item:hover {
  background: rgba(24, 144, 255, 0.1);
  color: var(--primary);
}

.tag-dropdown-item.active {
  color: var(--primary);
  background: rgba(24, 144, 255, 0.1);
}

.tag-dropdown-item .radio-icon {
  font-size: 10px;
  width: 14px;
  text-align: center;
}

.tag-dropdown-item.active .radio-icon {
  color: var(--primary);
}

.refresh-btn {
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--bg-card);
  border: 1px solid #e0e0e0;
  border-radius: var(--radius-full);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
  transition: var(--transition);
}

.refresh-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.tag-filter-bar {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 12px 8px;
  margin-bottom: 8px;
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tag-filter-bar::-webkit-scrollbar {
  display: none;
}

.filter-tag {
  flex: 0 0 auto;
  padding: 6px 12px;
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--bg);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.filter-tag:hover {
  color: var(--primary);
  background: rgba(24, 144, 255, 0.1);
}

.filter-tag.active {
  color: #fff;
  background: var(--primary);
}

.filter-tag.more-btn {
  color: var(--primary);
  background: rgba(24, 144, 255, 0.1);
  font-weight: 500;
}

.refresh-btn .spinning {
  display: inline-block;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.sidebar {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-section {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 20px;
  box-shadow: var(--shadow);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 6px 12px;
  font-size: 13px;
  color: var(--primary);
  background: rgba(24, 144, 255, 0.1);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: var(--transition);
}

.tag-item:hover {
  background: rgba(24, 144, 255, 0.2);
}

.show-more {
  margin-top: 12px;
  text-align: center;
  font-size: 13px;
  color: var(--primary);
  cursor: pointer;
}

.empty-tip {
  text-align: center;
  padding: 20px;
  color: var(--text-light);
  font-size: 14px;
}

.hot-user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s ease;
}

.hot-user-item:hover {
  background: var(--bg);
}

.user-rank {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: #ccc;
  border-radius: 50%;
}

.user-rank.rank-1 {
  background: #ffd700;
}

.user-rank.rank-2 {
  background: #c0c0c0;
}

.user-rank.rank-3 {
  background: #cd7f32;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 13px;
  color: var(--text);
}

.user-likes {
  font-size: 12px;
  color: var(--text-light);
}

.loading-state {
  text-align: center;
  padding: 40px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--bg);
  border-top: 3px solid var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

.loading-state p {
  color: var(--text-light);
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: var(--bg-card);
  border-radius: var(--radius);
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
}

.empty-state .sub-text {
  font-size: 14px;
  color: var(--text-light);
  margin-top: 8px;
}

.end-tip {
  text-align: center;
  padding: 30px;
  color: #bbb;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.page-btn {
  padding: 8px 16px;
  font-size: 14px;
  color: var(--text);
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}

.page-btn:disabled {
  color: #bbb;
  cursor: not-allowed;
  opacity: 0.6;
}

.page-info {
  font-size: 14px;
  color: var(--text-secondary);
}

@media (max-width: 900px) {
  .sidebar {
    display: none;
  }
  
  .feed-column {
    max-width: 100%;
  }
}

@media (max-width: 600px) {
  .main-container {
    padding: 0;
  }
  
  .feed-header {
    padding: 16px;
  }
}
</style>