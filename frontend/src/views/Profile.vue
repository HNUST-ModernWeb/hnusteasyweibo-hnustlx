<template>
  <div class="profile-page">
    <div class="profile-header">
      <button class="back-btn" @click="$router.back()">
        <span>←</span>
      </button>
      <h2>个人主页</h2>
      <div class="header-placeholder"></div>
    </div>
    
    <div class="profile-cover">
      <div class="cover-gradient"></div>
    </div>
    
    <div class="profile-info">
      <img :src="userInfo.avatar || defaultAvatar" class="profile-avatar" />
      <div class="user-details">
        <h1 class="username">{{ userInfo.username }}</h1>
        <p class="bio">{{ userInfo.bio || '这个人很懒，什么都没写~' }}</p>
      </div>
      <button v-if="isOwner" class="edit-btn" @click="showEdit = true">编辑资料</button>
    </div>
    
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-num">{{ stats.postCount }}</span>
        <span class="stat-label">动态</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ stats.likeCount }}</span>
        <span class="stat-label">获赞</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ stats.commentCount }}</span>
        <span class="stat-label">评论</span>
      </div>
    </div>
    
    <div class="profile-tabs">
      <button 
        :class="['tab-btn', { active: activeTab === 'post' }]"
        @click="activeTab = 'post'"
      >
        {{ isOwner ? '我的动态' : '动态' }}
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'like' }]"
        @click="activeTab = 'like'"
      >
        {{ isOwner ? '点赞' : '赞' }}
      </button>
    </div>
    
    <div class="content-area">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="activeTab === 'post'">
        <PostItem 
          v-for="post in posts" 
          :key="post.postId" 
          :post="post" 
          @click="goDetail(post.postId)"
          @refresh="fetchUserPosts"
        />
        <div v-if="posts.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <p>还没有发布过动态</p>
        </div>
      </div>
      
      <div v-else>
        <div 
          v-for="like in likes" 
          :key="like.likeId" 
          class="like-item"
          @click="goDetail(like.postId)"
        >
          <div class="like-content">
            <span class="like-user">你赞了</span>
            <span class="like-text">{{ like.postContent }}</span>
          </div>
          <span class="like-time">{{ formatTime(like.createTime) }}</span>
        </div>
        <div v-if="likes.length === 0" class="empty-state">
          <div class="empty-icon">❤️</div>
          <p>还没有点赞过</p>
        </div>
      </div>
    </div>
    
    <div v-if="showEdit" class="edit-modal">
      <div class="modal-content">
        <h3>编辑资料</h3>
        <div class="form-group">
          <label for="username">用户名</label>
          <input id="username" name="username" v-model="editForm.username" maxlength="15" autocomplete="username" style="width: 100%; padding: 14px 16px; border: 1px solid var(--border); border-radius: var(--radius); font-size: 15px; color: var(--text); background: var(--bg-card); appearance: none; -webkit-appearance: none; min-height: 48px; box-sizing: border-box;" />
        </div>
        <div class="form-group">
          <label>头像</label>
          <div class="avatar-upload">
            <img :src="editForm.avatar || defaultAvatar" class="preview-avatar" />
            <label for="avatar-upload-input" class="upload-label">点击更换头像</label>
            <input id="avatar-upload-input" type="file" accept="image/*" style="display: none;" @change="handleAvatarChange" />
          </div>
        </div>
        <div class="form-group">
          <label for="bio">个人简介</label>
          <textarea id="bio" name="bio" v-model="editForm.bio" maxlength="200" placeholder="说点什么介绍自己" rows="3" style="width: 100%; padding: 12px 16px; border: 1px solid var(--border); border-radius: var(--radius); font-size: 15px; color: var(--text); background: var(--bg-card); resize: vertical; box-sizing: border-box; font-family: inherit; line-height: 1.5;" />
          <span class="char-count">{{ editForm.bio?.length || 0 }}/200</span>
        </div>
        <div class="modal-actions">
          <button class="cancel-btn" @click="showEdit = false">取消</button>
          <button class="save-btn" @click="saveProfile">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi, postApi, likeApi } from '../api'
import { notify } from '../utils/notify'
import NavBar from '../components/NavBar.vue'
import PostItem from '../components/PostItem.vue'

const route = useRoute()
const router = useRouter()

const userInfo = ref({})
const posts = ref([])
const likes = ref([])
const loading = ref(false)
const activeTab = ref('post')
const showEdit = ref(false)
const editForm = ref({ username: '', avatar: '', bio: '' })

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
const stats = ref({ postCount: 0, likeCount: 0, commentCount: 0 })

const isOwner = computed(() => {
  const currentUserId = localStorage.getItem('userId')
  return currentUserId && parseInt(currentUserId) === parseInt(route.params.userId)
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN')
}

const fetchUserInfo = async () => {
  try {
    const targetUserId = route.params.userId
    console.log('route.params.userId:', targetUserId)
    console.log('isOwner:', isOwner.value)
    
    // 统一使用 getInfoById 接口，传递 userId 参数
    const res = await userApi.getInfoById(targetUserId)
    console.log('getInfoById 返回:', res)
    userInfo.value = res.data || {}
    
    console.log('userInfo:', userInfo.value)
  } catch (e) { 
    console.error(e) 
  }
}

const fetchUserPosts = async () => {
  loading.value = true
  try {
    const targetUserId = parseInt(route.params.userId)
    const res = await postApi.list({ page: 1, pageSize: 100 })
    posts.value = (res.data?.records || []).filter(p => p.userId === targetUserId)
    stats.value.postCount = posts.value.length
    stats.value.commentCount = posts.value.reduce((sum, p) => sum + (p.commentCount || 0), 0)
    
    const likesRes = await postApi.getUserTotalLikes(targetUserId)
    stats.value.likeCount = likesRes.data || 0
  } catch (e) { console.error(e) } finally {
    loading.value = false
  }
}

const fetchLikes = async () => {
  if (!isOwner.value) return
  loading.value = true
  try {
    const res = await likeApi.myList({ page: 1, pageSize: 50 })
    likes.value = res.data?.records || []
  } catch (e) { console.error(e) } finally {
    loading.value = false
  }
}

const goDetail = (postId) => {
  router.push(`/post/${postId}`)
}

const handleAvatarChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  const reader = new FileReader()
  reader.onload = (ev) => {
    editForm.value.avatar = ev.target.result
  }
  reader.readAsDataURL(file)
}

const saveProfile = async () => {
  try {
    // 检查 username 是否有修改
    const currentUsername = localStorage.getItem('username') || ''
    const hasUsernameChange = editForm.value.username && editForm.value.username !== currentUsername
    
    // 只在 username 有修改时调用 update
    if (hasUsernameChange) {
      await userApi.update({ 
        username: editForm.value.username, 
        bio: editForm.value.bio 
      })
    } else if (editForm.value.bio !== undefined) {
      await userApi.update({ bio: editForm.value.bio })
    }
    
    // 只在选择了新头像时上传
    if (editForm.value.avatar && editForm.value.avatar.startsWith('data:')) {
      const formData = new FormData()
      const res = await fetch(editForm.value.avatar)
      const blob = await res.blob()
      formData.append('avatar', blob, 'avatar.jpg')
      const avatarRes = await userApi.uploadAvatar(formData)
      const newAvatar = avatarRes.data || avatarRes
      localStorage.setItem('avatar', newAvatar)
      userInfo.value.avatar = newAvatar
    }
    
    showEdit.value = false
    fetchUserInfo()
    notify.success('保存成功')
  } catch (e) { 
    console.error(e)
    notify.error('保存失败')
  }
}

onMounted(() => {
  fetchUserInfo()
  fetchUserPosts()
  if (isOwner.value) {
    fetchLikes()
    editForm.value = { 
      username: localStorage.getItem('username') || '',
      avatar: localStorage.getItem('avatar') || ''
    }
  }
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--bg);
}

.profile-header {
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
  width: 36px;
  height: 36px;
  border: none;
  background: var(--bg);
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  transition: var(--transition);
}

.back-btn:hover {
  background: #e0e0e0;
}

.profile-header h2 {
  font-size: 17px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
}

.header-placeholder {
  width: 36px;
}

.profile-cover {
  height: 150px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-light) 100%);
  position: relative;
}

.cover-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100px;
  background: linear-gradient(transparent, #f8f9fa);
}

.profile-info {
  max-width: 700px;
  margin: -50px auto 0;
  padding: 0 20px;
  display: flex;
  align-items: flex-end;
  gap: 20px;
  position: relative;
}

.profile-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 4px solid var(--bg-card);
  object-fit: cover;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.user-details {
  flex: 1;
  padding-bottom: 10px;
}

.username {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.bio {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.edit-btn {
  padding: 10px 24px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.edit-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.stats-bar {
  max-width: 700px;
  margin: 30px auto;
  display: flex;
  justify-content: space-around;
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 20px;
  box-shadow: var(--shadow);
}

.stat-item {
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.profile-tabs {
  max-width: 700px;
  margin: 0 auto;
  display: flex;
  background: var(--bg-card);
  border-radius: var(--radius) var(--radius) 0 0;
  box-shadow: var(--shadow);
  overflow: hidden;
}

.tab-btn {
  flex: 1;
  padding: 16px;
  border: none;
  background: transparent;
  font-size: 15px;
  color: #999;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.tab-btn.active {
  color: #1890ff;
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 3px;
  background: linear-gradient(135deg, #1890ff, #1890ff);
  border-radius: 3px 3px 0 0;
}

.content-area {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}

.loading-state {
  text-align: center;
  padding: 40px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #fff;
  border-radius: 16px;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #999;
  margin: 0;
}

.like-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.like-item:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.like-user {
  display: block;
  font-size: 12px;
  color: #1890ff;
  margin-bottom: 6px;
}

.like-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.like-time {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.edit-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: var(--bg-card);
  border-radius: var(--radius);
  padding: 24px;
  width: 90%;
  max-width: 400px;
  box-shadow: var(--shadow);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { 
    opacity: 0;
    transform: translateY(20px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-content h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  text-align: center;
  color: var(--text);
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #fff;
  margin-bottom: 8px;
}

:deep(.form-group input[type="text"]) {
  width: 100% !important;
  padding: 14px 16px !important;
  border: 1px solid var(--border) !important;
  border-radius: var(--radius) !important;
  font-size: 15px !important;
  color: var(--text) !important;
  background: var(--bg-card) !important;
  appearance: none !important;
  -webkit-appearance: none !important;
  -moz-appearance: none !important;
  min-height: 48px !important;
  box-sizing: border-box !important;
}

.form-group input[type="text"]:focus {
  outline: none !important;
  border-color: var(--primary) !important;
  box-shadow: 0 0 0 4px rgba(24, 144, 255, 0.1),
              0 4px 12px rgba(24, 144, 255, 0.15) !important;
  background: var(--bg-card) !important;
}

.form-group input[type="text"]::placeholder {
  color: var(--text-light);
  font-size: 14px;
}

.form-group input[type="text"]:hover:not(:focus) {
  border-color: var(--text-light);
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.upload-label {
  display: inline-block;
  padding: 8px 20px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 50%, #69c0ff 100%);
  border-radius: var(--radius);
  cursor: pointer;
  transition: var(--transition);
  font-weight: 500;
  font-size: 14px;
  color: #fff;
  margin-bottom: 8px;
  border: none;
}

.upload-label:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.upload-label:active {
  transform: translateY(0);
}

.preview-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--border);
  transition: all 0.3s ease;
  cursor: pointer;
}

.preview-avatar:hover {
  border-color: var(--primary);
  transform: scale(1.05);
}

.avatar-upload input[type="file"] {
  font-size: 13px;
  color: var(--text-secondary);
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.cancel-btn, .save-btn {
  flex: 1;
  padding: 10px;
  border-radius: var(--radius);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn {
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  border: 1px solid #d9d9d9;
  color: #666;
  font-weight: 500;
}

.cancel-btn:hover {
  background: linear-gradient(135deg, #e8e8e8 0%, #d9d9d9 100%);
  color: #333;
  transform: translateY(-2px);
}

.cancel-btn:active {
  transform: translateY(0);
}

.save-btn {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 50%, #69c0ff 100%);
  border: none;
  color: #fff;
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.save-btn:hover {
  background: linear-gradient(135deg, #40a9ff 0%, #69c0ff 50%, #96c2ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.4);
}

.save-btn:active {
  transform: translateY(0);
}

@media (max-width: 600px) {
  .profile-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .profile-avatar {
    width: 80px;
    height: 80px;
  }
  
  .user-details {
    padding-bottom: 0;
  }
  
  .edit-btn {
    position: absolute;
    top: -60px;
    right: 20px;
  }
}

.char-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: var(--text-light);
  margin-top: 4px;
}
</style>