<template>
  <div class="chat-page">
    <NavBar />
    
    <main class="chat-container">
      <div class="chat-header">
        <button class="back-btn" @click="goBack">←</button>
        <div class="group-title">
          <img :src="groupAvatar || defaultGroupAvatar" class="group-avatar" @click="showAvatarModal = true" />
          <h2>{{ groupName }}</h2>
        </div>
        <button class="info-btn" @click="showMembers = !showMembers">👥</button>
        <button class="invite-btn" @click="toggleInvite">邀请</button>
      </div>
      
      <div ref="messagesContainer" class="messages-wrapper">
        <template v-for="(msg, index) in messages" :key="msg.messageId">
          <div v-if="index > 0 && showTimeDivider(msg.sendTime, messages[index - 1].sendTime)" class="time-divider">
            {{ formatFullTime(msg.sendTime) }}
          </div>
          <div 
            :class="['message-bubble', { mine: msg.senderId === currentUserId }]"
          >
            <img v-if="msg.senderId !== currentUserId" :src="msg.senderAvatar || defaultAvatar" class="avatar" />
            <div class="message-body">
              <span v-if="msg.senderId !== currentUserId" class="sender-name">{{ msg.senderName }}</span>
              <div class="bubble-content">{{ msg.content }}</div>
            </div>
            <img v-if="msg.senderId === currentUserId" :src="msg.senderAvatar || defaultAvatar" class="avatar" />
          </div>
        </template>
        
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
        
        <h3 style="margin-top: 20px">可邀请好友</h3>
        <div 
          v-for="friend in allFriends" 
          :key="friend.userId"
          class="member-item"
          @click="inviteFriend(friend.userId)"
        >
          <img :src="friend.avatar || defaultAvatar" class="avatar" />
          <span>{{ friend.username }}</span>
          <span class="invite-hint">邀请</span>
        </div>
        <p v-if="allFriends.length === 0" class="empty-tip">暂无可邀请的好友</p>
      </div>
    </div>

    <!-- 修改群头像 -->
    <div v-if="showAvatarModal" class="members-panel" @click.self="showAvatarModal = false">
      <div class="members-list">
        <h3>修改群头像</h3>
        <div class="avatar-picker-section">
          <img :src="selectedAvatar || groupAvatar || defaultGroupAvatar" class="current-avatar" />
          <label class="upload-avatar-btn">
            <input type="file" accept="image/*" @change="handleAvatarUpload" />
            上传图片
          </label>
        </div>
        <p class="divider-text">或选择预设头像</p>
        <div class="avatar-grid">
          <img 
            v-for="avatar in avatarOptions" 
            :key="avatar"
            :src="avatar"
            class="avatar-option"
            :class="{ selected: selectedAvatar === avatar }"
            @click="selectedAvatar = avatar"
          />
        </div>
        <div class="modal-actions">
          <button class="cancel-btn" @click="showAvatarModal = false">取消</button>
          <button class="save-btn" @click="updateGroupAvatar">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { chatApi, friendApi } from '../api'
import api from '../api'
import { notify } from '../utils/notify'
import { wsService } from '../utils/websocket'
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
const currentUserId = ref(null)
const showMembers = ref(false)
const showInviteFriends = ref(false)
const allFriends = ref([])
const groupSubscription = ref(null)
const showAvatarModal = ref(false)
const selectedAvatar = ref('')
const groupAvatar = ref('')
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

const groupId = computed(() => parseInt(route.params.groupId))

const fetchMessages = async () => {
  try {
    const res = await chatApi.groupMessageList(groupId.value, { page: 1, pageSize: 50 })
    messages.value = res.data?.records || []
    console.log('获取消息:', messages.value.length, messages.value.map(m => ({ id: m.messageId, time: m.sendTime })))
    scrollToBottom()
  } catch (e) {
    console.error('获取消息失败:', e)
  }
}

const fetchGroupInfo = async () => {
  try {
    const infoRes = await chatApi.getGroupInfo(groupId.value)
    const info = infoRes.data
    groupName.value = info?.groupName || '群聊'
    groupAvatar.value = info?.avatar || defaultGroupAvatar
  } catch (e) {
    console.error('获取群信息失败:', e)
  }
}

watch(showMembers, (val) => {
  if (val && members.value.length === 0) {
    fetchMembers()
  }
})

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
    await fetchMessages()
    nextTick(scrollToBottom)
  } catch (e) {
    notify.error('发送失败: ' + (e.response?.data?.msg || e.message))
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
    const [friendsRes, membersRes] = await Promise.all([
      friendApi.list({ page: 1, pageSize: 50 }),
      chatApi.groupMembers(groupId.value, { page: 1, pageSize: 100 })
    ])
    const friends = friendsRes.data?.records || []
    const members = membersRes.data?.records || []
    const memberIds = members.map(m => m.userId)
    allFriends.value = friends.filter(f => !memberIds.includes(f.userId))
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

const updateGroupAvatar = async () => {
  if (selectedAvatar.value && selectedAvatar.value.startsWith('data:')) {
    const formData = new FormData()
    const res = await fetch(selectedAvatar.value)
    const blob = await res.blob()
    formData.append('avatar', blob, 'avatar.jpg')
    try {
      const avatarRes = await api.post(`/group/${groupId.value}/avatar`, formData)
      groupAvatar.value = avatarRes.data || avatarRes
      notify.success('群头像已更新')
    } catch (e) {
      notify.error(e.response?.data?.msg || '上传失败')
    }
  } else if (selectedAvatar.value) {
    try {
      await chatApi.updateGroup(groupId.value, { 
        groupName: groupName.value, 
        avatar: selectedAvatar.value 
      })
      groupAvatar.value = selectedAvatar.value
      notify.success('群头像已更新')
    } catch (e) {
      notify.error(e.response?.data?.msg || '更新失败')
    }
  }
  showAvatarModal.value = false
}

const handleAvatarUpload = async (e) => {
  const file = e.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (event) => {
      selectedAvatar.value = event.target.result
    }
    reader.readAsDataURL(file)
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const handleGroupMessage = (data) => {
  messages.value.push({
    messageId: data.messageId,
    groupId: data.groupId,
    senderId: data.senderId,
    senderName: data.senderName,
    senderAvatar: data.senderAvatar,
    content: data.content,
    sendTime: data.sendTime
  })
  scrollToBottom()
}

const formatTime = (time) => {
  if (!time) return ''
  const str = String(time)
  return str.includes('T') ? str.split('T')[1]?.slice(0, 5) : str.substring(11, 16)
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
  const diff = t1 - t2
  if (diff > 60000) console.log('显示时间分割线:', time, prevTime, diff)
  return diff > 60000
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  currentUserId.value = parseInt(localStorage.getItem('userId') || 0)
  await fetchGroupInfo()
  await fetchMessages()
  groupSubscription.value = wsService.subscribeGroup(groupId.value)
  wsService.on('group_message_' + groupId.value, handleGroupMessage)
})

onUnmounted(() => {
  if (groupSubscription.value) {
    wsService.unsubscribe(groupSubscription.value)
  }
  wsService.off('group_message_' + groupId.value, handleGroupMessage)
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

.message-body {
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
  word-break: break-word;
}

.message-bubble.mine .bubble-content {
  background: var(--primary);
  color: white;
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

.input-area {
  display: flex;
  align-items: center;
  gap: 12px;
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

.input-area input:focus {
  outline: none;
  border-color: var(--primary);
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

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 10px;
}

.avatar-option {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  object-fit: cover;
}

.avatar-option:hover {
  border-color: var(--primary);
}

.avatar-option.selected {
  border-color: var(--primary);
  background: rgba(24, 144, 255, 0.1);
}

.invite-hint {
  margin-left: auto;
  color: var(--primary);
  font-size: 12px;
}

.avatar-picker-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 0;
}

.current-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.upload-avatar-btn {
  display: inline-block;
  padding: 8px 16px;
  background: var(--primary);
  color: white;
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 14px;
}

.upload-avatar-btn input {
  display: none;
}

.divider-text {
  text-align: center;
  color: var(--text-light);
  font-size: 13px;
  margin: 10px 0;
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.cancel-btn, .save-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: var(--radius);
  font-size: 15px;
  cursor: pointer;
}

.cancel-btn {
  background: #eee;
  color: var(--text);
}

.save-btn {
  background: var(--primary);
  color: white;
}
</style>