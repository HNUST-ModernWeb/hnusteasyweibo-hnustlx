<template>
  <div class="friends-page">
    <NavBar />
    
    <main class="main-container">
      <div class="header">
        <h1>好友</h1>
        <button class="add-btn" @click="showAddModal = true">添加好友</button>
      </div>
      
      <div class="tabs">
        <span :class="['tab', { active: activeTab === 'list' }]" @click="activeTab = 'list'">好友列表</span>
        <span :class="['tab', { active: activeTab === 'requests' }]" @click="activeTab = 'requests'">
          添加请求
          <span v-if="requestCount > 0" class="badge">{{ requestCount }}</span>
        </span>
      </div>
      
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="activeTab === 'list'" class="friend-list">
        <div v-for="friend in friends" :key="friend.userId" class="friend-item" @click="goProfile(friend.userId)">
          <img :src="friend.avatar || defaultAvatar" class="avatar" />
          <div class="info">
            <div class="name">{{ friend.username }}</div>
            <div class="time">好友时间: {{ formatTime(friend.friendTime) }}</div>
          </div>
          <button class="chat-btn" @click.stop="goChat(friend.userId)">聊天</button>
        </div>
        <div v-if="friends.length === 0" class="empty">暂无好友，快去添加吧</div>
      </div>
      
      <div v-else class="request-list">
        <div v-for="req in requests" :key="req.userId" class="request-item">
          <img :src="req.avatar || defaultAvatar" class="avatar" />
          <div class="info">
            <div class="name">{{ req.username }}</div>
            <div class="time">申请时间: {{ formatTime(req.friendTime) }}</div>
          </div>
          <div class="actions">
            <button class="accept-btn" @click="acceptRequest(req.userId)">同意</button>
            <button class="reject-btn" @click="rejectRequest(req.userId)">拒绝</button>
          </div>
        </div>
        <div v-if="requests.length === 0" class="empty">暂无好友请求</div>
      </div>
    </main>
    
    <!-- 添加好友弹窗 -->
    <div v-if="showAddModal" class="modal-mask" @click.self="showAddModal = false">
      <div class="modal">
        <h3>添加好友</h3>
        <input v-model="searchKeyword" type="text" placeholder="搜索用户名" class="search-input" @input="searchUsers" />
        <div class="search-results">
          <div v-for="user in searchResults" :key="user.userId" class="user-item" @click="sendFriendRequest(user.userId)">
            <img :src="user.avatar || defaultAvatar" class="avatar" />
            <span>{{ user.username }}</span>
            <span class="add-text">添加</span>
          </div>
        </div>
        <button class="close-btn" @click="showAddModal = false">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { friendApi, searchApi } from '../api'
import NavBar from '../components/NavBar.vue'

const router = useRouter()
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const activeTab = ref('list')
const loading = ref(false)
const friends = ref([])
const requests = ref([])
const requestCount = ref(0)
const showAddModal = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])

const fetchFriends = async () => {
  loading.value = true
  try {
    const res = await friendApi.list({ page: 1, pageSize: 50 })
    friends.value = res.data?.records || []
  } catch (e) {
    console.error('获取好友列表失败:', e)
  } finally {
    loading.value = false
  }
}

const fetchRequests = async () => {
  try {
    const res = await friendApi.requests({ page: 1, pageSize: 20 })
    requests.value = res.data?.records || []
    requestCount.value = res.data?.total || 0
  } catch (e) {
    console.error('获取请求列表失败:', e)
  }
}

const searchUsers = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  try {
    const res = await searchApi.users(searchKeyword.value, 20)
    searchResults.value = res.data?.records || []
  } catch (e) {
    console.error('搜索失败:', e)
  }
}

const sendFriendRequest = async (userId) => {
  try {
    await friendApi.sendRequest(userId)
    alert('已发送好友申请')
    searchResults.value = searchResults.value.filter(u => u.userId !== userId)
  } catch (e) {
    alert(e.response?.data?.msg || '发送失败')
  }
}

const acceptRequest = async (userId) => {
  try {
    const req = requests.value.find(r => r.userId === userId)
    if (req) {
      await friendApi.accept(req.userId)
      await fetchRequests()
      await fetchFriends()
    }
  } catch (e) {
    alert('处理失败')
  }
}

const rejectRequest = async (userId) => {
  try {
    const req = requests.value.find(r => r.userId === userId)
    if (req) {
      await friendApi.reject(req.userId)
      await fetchRequests()
    }
  } catch (e) {
    alert('处理失败')
  }
}

const goProfile = (userId) => {
  router.push(`/profile/${userId}`)
}

const goChat = (userId) => {
  router.push(`/chat/private/${userId}`)
}

const formatTime = (time) => {
  if (!time) return ''
  return time.split('T')[0]
}

onMounted(() => {
  fetchFriends()
  fetchRequests()
})
</script>

<style scoped>
.friends-page {
  min-height: 100vh;
  background: var(--bg);
}

.main-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 16px;
  padding-bottom: 80px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header h1 {
  font-size: 24px;
  font-weight: 600;
}

.add-btn {
  padding: 8px 16px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}

.tabs {
  display: flex;
  gap: 16px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 16px;
}

.tab {
  padding: 12px 0;
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

.badge {
  font-size: 12px;
  padding: 2px 6px;
  background: red;
  color: white;
  border-radius: 10px;
  margin-left: 4px;
}

.friend-item, .request-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-card);
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
}

.info {
  flex: 1;
}

.name {
  font-weight: 500;
}

.time {
  font-size: 12px;
  color: var(--text-secondary);
}

.chat-btn, .accept-btn, .reject-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.chat-btn, .accept-btn {
  background: var(--primary);
  color: white;
}

.reject-btn {
  background: #999;
  color: white;
}

.actions {
  display: flex;
  gap: 8px;
}

.empty {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}

.loading {
  text-align: center;
  padding: 40px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: var(--bg-card);
  padding: 20px;
  border-radius: 12px;
  width: 80%;
  max-width: 400px;
}

.search-input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 12px;
}

.search-results {
  max-height: 300px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
}

.add-text {
  margin-left: auto;
  color: var(--primary);
}

.close-btn {
  width: 100%;
  padding: 12px;
  margin-top: 12px;
  border: none;
  border-radius: 8px;
  background: var(--bg);
  cursor: pointer;
}
</style>