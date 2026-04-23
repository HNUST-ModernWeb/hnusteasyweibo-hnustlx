<template>
  <div class="chat-page">
    <NavBar />
    
    <main class="chat-container">
      <div class="chat-header">
        <button class="back-btn" @click="goBack">←</button>
        <h2>{{ groupName }}</h2>
        <button class="info-btn" @click="showMembers = !showMembers">👥</button>
        <button class="invite-btn" @click="toggleInvite">邀请</button>
      </div>
      
      <div ref="messagesContainer" class="messages-wrapper">
        <div 
          v-for="msg in messages" 
          :key="msg.messageId"
          class="message-item"
        >
          <img :src="msg.senderAvatar || defaultAvatar" class="avatar" />
          <div class="message-info">
            <span class="sender-name">{{ msg.senderName }}</span>
            <div class="bubble-content">{{ msg.content }}</div>
            <span class="message-time">{{ formatTime(msg.sendTime) }}</span>
          </div>
        </div>
        
        <div v-if="messages.length === 0" class="empty-state">
          <p>群聊暂无消息</p>
        </div>
      </div>
      
      <div class="input-area">
        <input 
          v-model="inputMessage" 
          type="text" 
          placeholder="发送消息..."
          @keyup.enter="sendGroupMessage"
        />
        <button @click="sendGroupMessage" :disabled="!inputMessage.trim()">发送</button>
      </div>
    </main>
    
    <!-- 群成员列表 -->
    <div v-if="showMembers" class="members-panel" @click.self="showMembers = false">
      <div class="members-list">
        <h3>群成员 ({{ memberCount }})</h3>
        <div 
          v-for="member in members" 
          :key="member.userId"
          class="member-item"
        >
          <img :src="member.avatar || defaultAvatar" class="avatar" />
          <span>{{ member.username }}</span>
          <span v-if="member.role === 2" class="owner-tag">群主</span>
        </div>
        <button class="leave-btn" @click="leaveGroup">退出群聊</button>
      </div>
    </div>
    
    <!-- 邀请好友 -->
    <div v-if="showInviteFriends" class="members-panel" @click.self="showInviteFriends = false">
      <div class="members-list">
        <h3>邀请好友进群</h3>
        <div 
          v-for="friend in allFriends" 
          :key="friend.userId"
          class="member-item"
          @click="inviteFriend(friend.userId)"
        >
          <img :src="friend.avatar || defaultAvatar" class="avatar" />
          <span>{{ friend.username }}</span>
        </div>
        <p v-if="allFriends.length === 0" class="empty-tip">暂无好友</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { chatApi, friendApi } from '../api'
import { notify } from '../utils/notify'
import NavBar from '../components/NavBar.vue'

const router = useRouter()
const route = useRoute()
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const messagesContainer = ref(null)
const inputMessage = ref('')
const messages = ref([])
const members = ref([])
const groupName = ref('')
const memberCount = ref(0)
const showMembers = ref(false)
const showInviteFriends = ref(false)
const allFriends = ref([])

const groupId = computed(() => parseInt(route.params.groupId))

const fetchMessages = async () => {
  try {
    const res = await chatApi.groupMessageList(groupId.value, { page: 1, pageSize: 50 })
    messages.value = res.data?.records || []
    scrollToBottom()
  } catch (e) {
    console.error('获取消息失败:', e)
  }
}

const fetchGroupInfo = async () => {
  try {
    const res = await chatApi.groupMembers(groupId.value, { page: 1, pageSize: 1 })
    members.value = res.data?.records || []
    memberCount.value = res.data?.total || 0
    groupName.value = '群聊'
  } catch (e) {
    console.error('获取群信息失败:', e)
  }
}

const fetchMembers = async () => {
  try {
    const res = await chatApi.groupMembers(groupId.value, { page: 1, pageSize: 100 })
    members.value = res.data?.records || []
    memberCount.value = res.data?.total || 0
  } catch (e) {
    console.error('获取成员失败:', e)
  }
}

const sendGroupMessage = async () => {
  if (!inputMessage.value.trim()) return
  try {
    await chatApi.sendGroupMessage(groupId.value, { content: inputMessage.value.trim() })
    inputMessage.value = ''
    fetchMessages()
  } catch (e) {
    console.error('发送消息失败:', e)
  }
}

const leaveGroup = async () => {
  try {
    await chatApi.leaveGroup(groupId.value)
    router.push('/messages')
  } catch (e) {
    console.error('退出群聊失败:', e)
  }
}

const fetchFriends = async () => {
  try {
    const res = await friendApi.list({ page: 1, pageSize: 50 })
    allFriends.value = res.data?.records || []
  } catch (e) {
    console.error('获取好友列表失败:', e)
  }
}

const inviteFriend = async (userId) => {
  try {
    await chatApi.inviteToGroup(groupId.value, { userId })
    notify.success('已邀请好友')
    showInviteFriends.value = false
  } catch (e) {
    notify.error(e.response?.data?.msg || '邀请失败')
  }
}

const toggleInvite = async () => {
  showInviteFriends.value = !showInviteFriends.value
  if (showInviteFriends.value) {
    await fetchFriends()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  return time.split('T')[1]?.slice(0, 5) || ''
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  await fetchGroupInfo()
  await fetchMessages()
})
</script>

<style scoped>
.chat-page {
  min-height: 100vh;
  background: var(--bg);
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 600px;
  margin: 0 auto;
  width: 100%;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
}

.back-btn, .info-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text);
}

.chat-header h2 {
  flex: 1;
  font-size: 18px;
  color: var(--text);
}

.messages-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
}

.message-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sender-name {
  font-size: 12px;
  color: var(--primary);
  font-weight: 500;
}

.bubble-content {
  padding: 10px 14px;
  background: var(--bg-card);
  border-radius: 16px;
  font-size: 14px;
  color: var(--text);
  max-width: 240px;
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  color: var(--text-light);
}

.input-area {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: var(--bg-card);
  border-top: 1px solid var(--border);
}

.input-area input {
  flex: 1;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 20px;
  background: var(--bg);
  color: var(--text);
}

.input-area button {
  padding: 10px 20px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}

.input-area button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.members-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: flex-end;
}

.members-list {
  width: 280px;
  background: var(--bg-card);
  padding: 20px;
  overflow-y: auto;
}

.members-list h3 {
  margin-bottom: 16px;
  color: var(--text);
}

.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
  color: var(--text);
}

.owner-tag {
  font-size: 11px;
  padding: 2px 6px;
  background: var(--primary);
  color: white;
  border-radius: 4px;
}

.leave-btn {
  width: 100%;
  padding: 14px;
  margin-top: 20px;
  background: #ff4444;
  color: white;
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
}
</style>