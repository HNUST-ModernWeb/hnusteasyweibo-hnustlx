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
        <span 
          :class="['tab', { active: activeTab === 'friends' }]"
          @click="switchTab('friends')"
        >
          好友
        </span>
        <span 
          :class="['tab', { active: activeTab === 'group' }]"
          @click="switchTab('group')"
        >
          群聊
        </span>
      </div>
      
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
      </div>
      
      <!-- 点赞 -->
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
      
      <!-- 评论 -->
      <div v-else-if="activeTab === 'comment'" class="message-list">
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
      
      
      
      <!-- 好友 -->
      <div v-else-if="activeTab === 'friends'" class="message-list">
        <!-- 待处理申请 -->
        <div v-if="friendRequests.length > 0" class="request-section">
          <div class="section-title">待处理申请</div>
          <div 
            v-for="req in friendRequests" 
            :key="req.requestId"
            class="message-item"
          >
            <img :src="req.avatar || defaultAvatar" class="message-avatar" />
            <div class="message-content">
              <p class="message-text">
                <span class="user-name">{{ req.fromUsername }}</span>
              </p>
              <p class="message-time">{{ formatTime(req.createTime) }}</p>
            </div>
            <div class="request-actions">
              <button class="btn-accept" @click="acceptRequest(req.requestId)">接受</button>
              <button class="btn-reject" @click="rejectRequest(req.requestId)">拒绝</button>
            </div>
          </div>
        </div>
        
        <!-- 好友列表 -->
        <div class="section-title">好友列表</div>
        <div 
          v-for="friend in friends" 
          :key="friend.userId"
          class="message-item"
          @click="goPrivateChat(friend.userId)"
        >
          <img :src="friend.avatar || defaultAvatar" class="message-avatar" />
          <div class="message-content">
            <p class="message-text">
              <span class="user-name">{{ friend.username }}</span>
            </p>
            <p class="message-time">{{ formatTime(friend.friendTime) }}</p>
          </div>
          <button class="chat-btn-small" @click.stop="goPrivateChat(friend.userId)">聊天</button>
        </div>
        
        <div v-if="friends.length === 0" class="empty-state">
          <span class="empty-icon">👥</span>
          <p>暂无好友</p>
        </div>
        
        <button class="start-chat-btn" @click="showAddFriendModal = true">
          添加好友
        </button>
      </div>
      
      <!-- 群聊 -->
      <div v-else-if="activeTab === 'group'" class="message-list">
        <div 
          v-for="group in groupList" 
          :key="group.groupId"
          class="message-item"
          @click="goGroupChat(group.groupId)"
        >
          <img :src="group.avatar || defaultAvatar" class="message-avatar" />
          <div class="message-content">
            <p class="message-text">
              <span class="group-name">{{ group.groupName }}</span>
              <span class="member-count">({{ group.memberCount }}人)</span>
            </p>
            <p class="message-preview">{{ group.lastMessage || '暂无消息' }}</p>
            <span class="message-time">{{ formatTime(group.lastMessageTime) }}</span>
          </div>
        </div>
        
        <!-- 待处理邀请 -->
        <div v-if="groupInvites.length > 0" class="request-section">
          <div class="section-title">待处理群邀请</div>
          <div 
            v-for="invite in groupInvites" 
            :key="invite.inviteId"
            class="message-item"
          >
            <img :src="invite.inviterAvatar || defaultAvatar" class="message-avatar" />
            <div class="message-content">
              <p class="message-text">
                <span class="user-name">{{ invite.inviterName }}</span> 邀请你加入群聊
              </p>
              <p class="message-preview">{{ invite.groupName }}</p>
              <p class="message-time">{{ formatTime(invite.createTime) }}</p>
            </div>
            <div class="request-actions">
              <button class="btn-accept" @click="acceptGroupInvite(invite.inviteId)">接受</button>
              <button class="btn-reject" @click="rejectGroupInvite(invite.inviteId)">拒绝</button>
            </div>
          </div>
        </div>
        
        <div v-if="groupList.length === 0" class="empty-state">
          <span class="empty-icon">👥</span>
          <p>暂无群聊</p>
        </div>
        
        <button class="start-chat-btn" @click="openCreateGroupModal">
          创建群聊
        </button>
      </div>
    </main>
    
    <!-- 创建群聊弹窗 -->
    <div v-if="showCreateGroupModal" class="modal-mask" @click.self="showCreateGroupModal = false">
      <div class="modal">
        <h3>创建群聊</h3>
        <div class="avatar-picker">
          <img :src="newGroupAvatar || defaultGroupAvatar" class="modal-avatar" @click="showAvatarOptions = !showAvatarOptions" />
          <div v-if="showAvatarOptions" class="avatar-list">
            <img 
              v-for="avatar in avatarOptions" 
              :key="avatar"
              :src="avatar"
              class="avatar-option"
              :class="{ selected: newGroupAvatar === avatar }"
              @click="newGroupAvatar = avatar; showAvatarOptions = false"
            />
          </div>
        </div>
        <input 
          v-model="newGroupName" 
          type="text" 
          placeholder="输入群名称"
          class="modal-input"
        />
        <div class="modal-actions">
          <button @click="showCreateGroupModal = false; showAvatarOptions = false">取消</button>
          <button @click="handleCreateGroup" class="primary">创建</button>
        </div>
      </div>
    </div>
    
    <!-- 添加好友弹窗 -->
    <div v-if="showAddFriendModal" class="modal-mask" @click.self="showAddFriendModal = false">
      <div class="modal">
        <h3>添加好友</h3>
        <input 
          v-model="friendSearchKeyword" 
          type="text" 
          placeholder="搜索用户名"
          class="modal-input"
          @input="searchFriendUsers"
        />
        <div class="search-results">
          <div 
            v-for="user in friendSearchResults" 
            :key="user.userId" 
            class="user-item"
            @click="sendFriendRequest(user.userId)"
          >
            <img :src="user.avatar || defaultAvatar" class="avatar-small" style="width:36px;height:36px" />
            <span>{{ user.username }}</span>
            <span class="add-text">添加</span>
          </div>
        </div>
        <div class="modal-actions">
          <button @click="showAddFriendModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { likeApi, commentApi } from '../api'
import { notify } from '../utils/notify'
import NavBar from '../components/NavBar.vue'

const router = useRouter()
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})
api.interceptors.request.use(config => {
  const userToken = localStorage.getItem('token')
  if (userToken) {
    config.headers['user_token'] = userToken
  }
  return config
})
api.interceptors.response.use(response => response.data, error => {
  console.log('响应错误:', error)
  return Promise.reject(error)
})

const activeTab = ref('like')
const loading = ref(false)
const likes = ref([])
const comments = ref([])
const privateConversations = ref([])
const groupList = ref([])
const friends = ref([])
const friendRequests = ref([])
const groupInvites = ref([])
const showCreateGroupModal = ref(false)
const showAddFriendModal = ref(false)
const newGroupName = ref('')
const newGroupAvatar = ref('')
const showAvatarOptions = ref(false)
const friendSearchKeyword = ref('')
const friendSearchResults = ref([])
const defaultGroupAvatar = 'https://api.dicebear.com/7.x/identicon/svg?seed=group'
const avatarOptions = [
  'https://api.dicebear.com/7.x/identicon/svg?seed=family',
  'https://api.dicebear.com/7.x/identicon/svg?seed=team',
  'https://api.dicebear.com/7.x/identicon/svg?seed=work',
  'https://api.dicebear.com/7.x/identicon/svg?seed=study',
  'https://api.dicebear.com/7.x/identicon/svg?seed=sport',
  'https://api.dicebear.com/7.x/identicon/svg?seed=game',
  'https://api.dicebear.com/7.x/identicon/svg?seed=music',
  'https://api.dicebear.com/7.x/identicon/svg?seed=travel',
]

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

const fetchConversations = async () => {
  loading.value = true
  try {
    const res = await api.get('/message/private/conversations', { params: { page: 1, pageSize: 20 } })
    privateConversations.value = res.data?.records || []
  } catch (e) {
    console.error('获取会话列表失败:', e)
  } finally {
    loading.value = false
  }
}

const fetchGroupList = async () => {
  loading.value = true
  try {
    const [groupRes, inviteRes] = await Promise.all([
      api.get('/group/list', { params: { page: 1, pageSize: 20 } }),
      api.get('/group/invites', { params: { page: 1, pageSize: 20 } })
    ])
    groupList.value = groupRes.data?.records || []
    groupInvites.value = inviteRes.data?.records || []
  } catch (e) {
    console.error('获取群聊列表失败:', e)
  } finally {
    loading.value = false
  }
}

const acceptGroupInvite = async (inviteId) => {
  try {
    await api.post('/group/invite/accept', { inviteId })
    notify.success('已加入群聊')
    groupInvites.value = groupInvites.value.filter(i => i.inviteId !== inviteId)
  } catch (e) {
    notify.error(e.response?.data?.msg || '加入失败')
  }
}

const rejectGroupInvite = async (inviteId) => {
  try {
    await api.post('/group/invite/reject', { inviteId })
    groupInvites.value = groupInvites.value.filter(i => i.inviteId !== inviteId)
  } catch (e) {
    console.error('拒绝失败:', e)
  }
}

const goDetail = (postId) => {
  router.push(`/post/${postId}`)
}

const goPrivateChat = (userId) => {
  router.push(`/chat/private/${userId}`)
}

const goGroupChat = (groupId) => {
  router.push(`/chat/group/${groupId}`)
}

const createGroup = () => {
  newGroupName.value = ''
  showCreateGroupModal.value = true
}

const openCreateGroupModal = () => {
  newGroupName.value = ''
  showCreateGroupModal.value = true
}

const goSearch = () => {
  router.push('/search')
}

const handleCreateGroup = async () => {
  const name = newGroupName.value.trim()
  if (!name) {
    notify.warning('请输入群名称')
    return
  }
  try {
    const res = await api.post('/group/create', { 
      groupName: name, 
      avatar: newGroupAvatar.value || defaultGroupAvatar 
    })
    if (res.code === 200) {
      showCreateGroupModal.value = false
      newGroupName.value = ''
      newGroupAvatar.value = ''
      showAvatarOptions.value = false
      fetchGroupList()
      fetchGroupList()
    } else {
      notify.success(res.msg || '创建成功')
    }
  } catch (e) {
    console.error('创建群聊失败:', e)
    notify.error('创建失败: ' + (e.response?.data?.msg || e.message))
  }
}

const fetchFriends = async () => {
  loading.value = true
  try {
    const [friendRes, reqRes] = await Promise.all([
      api.get('/friend/list', { params: { page: 1, pageSize: 50 } }),
      api.get('/friend/requests', { params: { page: 1, pageSize: 50 } })
    ])
    console.log('好友响应:', friendRes)
    console.log('申请响应:', reqRes)
    friends.value = friendRes.data?.records || []
    friendRequests.value = reqRes.data?.records || []
    console.log('friendRequests:', friendRequests.value)
  } catch (e) {
    console.error('获取好友列表失败:', e)
  } finally {
    loading.value = false
  }
}

const acceptRequest = async (requestId) => {
  try {
    await api.post('/friend/accept', { requestId })
    notify.success('已添加好友')
    fetchFriends()
    friendSearchResults.value = friendSearchResults.value.filter(u => u.userId !== userId)
  } catch (e) {
    notify.error(e.response?.data?.msg || '添加失败')
  }
}

const sendFriendRequest = async (userId) => {
  try {
    await api.post('/friend/request', { userId })
    notify.success('已发送好友申请')
    friendSearchResults.value = friendSearchResults.value.filter(u => u.userId !== userId)
  } catch (e) {
    notify.error(e.response?.data?.msg || '发送失败')
  }
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
  } else if (tab === 'comment') {
    fetchComments()
  } else if (tab === 'private') {
    fetchConversations()
  } else if (tab === 'friends') {
    fetchFriends()
  } else if (tab === 'group') {
    fetchGroupList()
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
  gap: 12px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--border);
  overflow-x: auto;
}

.tab {
  padding: 12px 0;
  font-size: 15px;
  color: var(--text-secondary);
  cursor: pointer;
  position: relative;
  white-space: nowrap;
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
  object-fit: cover;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-text {
  font-size: 14px;
  color: var(--text);
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 500;
  color: var(--primary);
}

.group-name {
  font-weight: 500;
}

.member-count {
  font-size: 12px;
  color: var(--text-secondary);
}

.unread-badge {
  font-size: 11px;
  padding: 2px 6px;
  background: var(--primary);
  color: white;
  border-radius: 10px;
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

.start-chat-btn {
  width: 100%;
  padding: 14px;
  margin-top: 16px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: var(--radius);
  font-size: 15px;
  cursor: pointer;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: var(--bg-card);
  padding: 24px;
  border-radius: var(--radius);
  width: 80%;
  max-width: 320px;
}

.modal h3 {
  margin-bottom: 16px;
  color: var(--text);
}

.modal-input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--bg);
  color: var(--text);
  margin-bottom: 16px;
}

.modal-actions {
  display: flex;
  gap: 12px;
}

.modal-actions button {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: var(--radius);
  font-size: 15px;
  cursor: pointer;
  background: var(--bg);
  color: var(--text);
}

.modal-actions button.primary {
  background: var(--primary);
  color: white;
}

.chat-btn-small {
  padding: 6px 12px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.search-results {
  max-height: 250px;
  overflow-y: auto;
  margin-bottom: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
}

.user-item:hover {
  background: var(--bg);
}

.avatar-small {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.add-text {
  margin-left: auto;
  color: var(--primary);
  font-size: 13px;
}

.request-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 13px;
  color: var(--text-light);
  padding: 8px 0 4px;
}

.request-actions {
  display: flex;
  gap: 6px;
}

.btn-accept, .btn-reject {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.btn-accept {
  background: var(--primary);
  color: white;
}

.btn-reject {
  background: #eee;
  color: var(--text);
}

.avatar-picker {
  position: relative;
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.modal-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid var(--border);
}

.avatar-list {
  position: absolute;
  top: 70px;
  left: 50%;
  transform: translateX(-50%);
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  background: var(--bg-card);
  padding: 12px;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  z-index: 10;
  width: 280px;
}

.avatar-option {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.avatar-option:hover {
  border-color: var(--primary);
}

.avatar-option.selected {
  border-color: var(--primary);
  background: rgba(24, 144, 255, 0.1);
}
</style>