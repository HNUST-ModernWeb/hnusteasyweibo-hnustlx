<template>
  <div class="chat-page">
    <NavBar />
    
    <main class="chat-container">
      <div class="chat-header">
        <button class="back-btn" @click="goBack">←</button>
        <h2>{{ chatUser?.username || '聊天' }}</h2>
      </div>
      
      <div ref="messagesContainer" class="messages-wrapper">
        <template v-for="(msg, index) in messages" :key="msg.messageId">
          <div v-if="index > 0 && showTimeDivider(msg.sendTime, messages[index - 1].sendTime)" class="time-divider">
            {{ formatFullTime(msg.sendTime) }}
          </div>
          <div 
            :class="['message-bubble', { mine: msg.senderId === currentUserId }]"
          >
            <img v-if="msg.senderId !== currentUserId" :src="msg.senderAvatar || chatUser?.avatar || defaultAvatar" class="avatar" />
            <div class="bubble-content">{{ msg.content }}</div>
            <img v-if="msg.senderId === currentUserId" :src="msg.senderAvatar || currentUser?.avatar || defaultAvatar" class="avatar" />
          </div>
        </template>
        
        <div v-if="messages.length === 0" class="empty-state">
          <p>开始对话吧</p>
        </div>
      </div>
      
      <div class="input-area">
        <input 
          v-model="inputMessage" 
          type="text" 
          placeholder="发送消息..."
          @keyup.enter="sendMessage"
        />
        <button @click="sendMessage" :disabled="!inputMessage.trim()">发送</button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { chatApi, userApi, friendApi } from '../api'
import { notify } from '../utils/notify'
import { wsService } from '../utils/websocket'
import NavBar from '../components/NavBar.vue'

const router = useRouter()
const route = useRoute()
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const messagesContainer = ref(null)
const inputMessage = ref('')
const messages = ref([])
const chatUser = ref(null)
const currentUserId = ref(null)
const currentUser = ref(null)

const userId = computed(() => parseInt(route.params.userId))

const handleNewMessage = (data) => {
  if (data.senderId === userId.value) {
    messages.value.push({
      messageId: data.messageId,
      senderId: data.senderId,
      receiverId: currentUserId.value,
      content: data.content,
      sendTime: data.sendTime
    })
    scrollToBottom()
  }
}

const fetchMessages = async () => {
  try {
    const res = await chatApi.privateList(userId.value, { page: 1, pageSize: 50 })
    messages.value = res.data?.records || []
    scrollToBottom()
  } catch (e) {
    console.error('获取消息失败:', e)
  }
}

const fetchUserInfo = async () => {
  try {
    const res = await userApi.getInfoById(userId.value)
    chatUser.value = res.data
  } catch (e) {
    console.error('获取用户信息失败:', e)
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  try {
    await chatApi.sendPrivate({
      receiverId: userId.value,
      content: inputMessage.value.trim()
    })
    inputMessage.value = ''
    await fetchMessages()
    nextTick(scrollToBottom)
  } catch (e) {
    notify.error('发送失败: ' + (e.response?.data?.msg || e.message))
  }
}

const formatFullTime = (time) => {
  if (!time) return ''
  const str = String(time).replace(' ', 'T')
  const date = new Date(str)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  if (msgDate.getTime() === today.getTime()) {
    return String(time).includes('T') 
      ? String(time).split('T')[1]?.slice(0, 5) 
      : String(time).substring(11, 16)
  }
  return String(time).includes('T')
    ? String(time).replace('T', ' ').substring(0, 16)
    : String(time).substring(0, 16)
}

const showTimeDivider = (time, prevTime) => {
  if (!time || !prevTime) return false
  const t1 = new Date(String(time).replace(' ', 'T')).getTime()
  const t2 = new Date(String(prevTime).replace(' ', 'T')).getTime()
  return t1 - t2 > 60000
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  currentUserId.value = parseInt(localStorage.getItem('userId') || 0)
  try {
    const res = await userApi.getCurrentUserInfo()
    currentUser.value = res.data
  } catch (e) {}
  try {
    await friendApi.isFriend(userId.value)
  } catch (e) {
    notify.warning('你们还不是好友，无法发起私聊')
    router.replace('/messages')
    return
  }
  wsService.on('private_message', handleNewMessage)
  await fetchUserInfo()
  await fetchMessages()
})

onUnmounted(() => {
  wsService.off('private_message', handleNewMessage)
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

.back-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text);
}

.chat-header h2 {
  font-size: 18px;
  color: var(--text);
}

.messages-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-bubble {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 75%;
}

.message-bubble.mine {
  align-self: flex-end;
  flex-direction: row;
}

.message-bubble:not(.mine) {
  align-self: flex-start;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
}

.bubble-content {
  padding: 10px 14px;
  background: var(--bg-card);
  border-radius: 16px;
  font-size: 14px;
  color: var(--text);
  word-break: break-word;
}

.message-bubble.mine .bubble-content {
  background: var(--primary);
  color: white;
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

.time-divider {
  text-align: center;
  font-size: 12px;
  color: var(--text-light);
  padding: 8px 0;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}
</style>