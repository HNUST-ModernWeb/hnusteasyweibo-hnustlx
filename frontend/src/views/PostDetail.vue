<template>
  <div class="detail-page">
    <div class="detail-header">
      <button class="back-btn" @click="$router.back()">
        <span>←</span>
      </button>
      <h2>动态详情</h2>
      <div class="header-placeholder"></div>
    </div>
    
    <div class="detail-content">
      <div class="post-card">
        <div class="post-header">
          <img 
            :src="post.avatar || defaultAvatar" 
            class="avatar" 
            @click="goProfile"
          />
          <div class="user-info">
            <span class="username" @click="goProfile">{{ post.username }}</span>
            <span class="time">{{ formatTime(post.createTime) }}</span>
          </div>
          <button v-if="isOwner" class="more-btn" @click="showMenu = !showMenu">
            ⋮
          </button>
          <div v-if="showMenu && isOwner" class="action-menu">
            <div class="menu-item" @click="handleDelete">删除</div>
          </div>
        </div>
        
        <div class="post-text">{{ post.content }}</div>
        
        <div v-if="postTags.length" class="post-tags">
          <span v-for="tag in postTags" :key="tag.tagId" class="tag">
            #{{ tag.tagName }}
          </span>
        </div>
        
        <div v-if="isOwner" class="edit-tags-btn" @click="openTagEditor">
          编辑标签
        </div>
        
        <div v-if="post.images && post.images.length" class="post-images">
          <img 
            v-for="(img, idx) in post.images" 
            :key="idx" 
            :src="img" 
            class="post-image"
            @click="previewImage(idx)"
          />
        </div>
        
        <div class="post-stats">
          <span v-if="post.likeCount > 0">{{ post.likeCount }} 赞</span>
          <span v-if="post.commentCount > 0">{{ post.commentCount }} 评论</span>
        </div>
        
        <div class="post-actions">
          <button 
            :class="['action-btn', { active: post.isLiked }]" 
            @click="handleLike"
          >
            <span class="icon">{{ post.isLiked ? '❤️' : '🤍' }}</span>
            <span>{{ post.isLiked ? '已赞' : '点赞' }}</span>
          </button>
          <button class="action-btn" @click="focusComment">
            <span class="icon">💬</span>
            <span>评论</span>
          </button>
          <button class="action-btn" @click="sharePost">
            <span class="icon">🔗</span>
            <span>分享</span>
          </button>
        </div>
      </div>
      
      <div class="comment-section">
        <h3>评论 {{ comments.length ? `(${comments.length})` : '' }}</h3>
        
        <div class="comment-list">
          <div 
            v-for="comment in comments" 
            :key="comment.commentId" 
            class="comment-item"
          >
            <img :src="comment.avatar || defaultAvatar" class="comment-avatar" />
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-username">{{ comment.username }}</span>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
                <button 
                v-if="currentUserId && currentUserId === comment.userId"
                class="delete-comment"
                @click="deleteComment(comment.commentId)"
              >
                删除
              </button>
            </div>
          </div>
          
          <div v-if="comments.length === 0" class="empty-comments">
            <div class="empty-icon">💬</div>
            <p>还没有评论</p>
            <p class="sub-text">快来抢沙发吧</p>
          </div>
        </div>
      </div>
    </div>
    
    <div class="comment-input-bar" :class="{ focused: inputFocused }">
      <input 
        ref="commentInput"
        v-model="commentText"
        placeholder="说点什么..."
        @focus="inputFocused = true"
        @blur="inputFocused = false"
        @keyup.enter="submitComment"
      />
      <button 
        class="send-btn" 
        :class="{ active: commentText.trim() }"
        @click="submitComment"
      >
        发送
      </button>
    </div>
    
    <div v-if="showPreview" class="image-preview" @click="showPreview = false">
      <img :src="previewUrl" class="preview-img" @click.stop />
    </div>
    
    <div v-if="showTagEditor" class="tag-editor-overlay" @click="showTagEditor = false">
      <div class="tag-editor-panel" @click.stop>
        <div class="tag-editor-header">
          <h3>编辑标签</h3>
          <button class="close-btn" @click="showTagEditor = false">✕</button>
        </div>
        
        <div class="tag-selector">
          <div class="tag-list">
            <span 
              v-for="tag in availableTags" 
              :key="tag.tagId"
              :class="['tag-item', { active: isTagSelected(tag.tagId) }]"
              @click="toggleTag(tag)"
            >
              {{ tag.tagName }}
            </span>
          </div>
          
          <div class="tag-tip">已选择: {{ selectedTags.length }}/5</div>
        </div>
        
        <div class="tag-editor-footer">
          <button class="cancel-btn" @click="showTagEditor = false">取消</button>
          <button class="save-btn" @click="saveTags">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postApi, commentApi, likeApi, postTagApi, tagApi } from '../api'

const route = useRoute()
const router = useRouter()

const post = ref({})
const comments = ref([])
const commentText = ref('')
const showMenu = ref(false)
const showPreview = ref(false)
const previewUrl = ref('')
const inputFocused = ref(false)
const commentInput = ref(null)
const currentUserId = ref(null)

const postTags = ref([])
const availableTags = ref([])
const selectedTags = ref([])
const showTagEditor = ref(false)

onMounted(() => {
  currentUserId.value = parseInt(localStorage.getItem('userId')) || null
})

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const isOwner = computed(() => {
  const userId = localStorage.getItem('userId')
  return userId && parseInt(userId) === post.value.userId
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString('zh-CN')
}

const fetchDetail = async () => {
  try {
    const res = await postApi.detail(route.params.postId)
    post.value = res.data || {}
  } catch (e) { console.error(e) }
}

const fetchComments = async () => {
  try {
    const res = await commentApi.list(route.params.postId, { page: 1, pageSize: 50 })
    comments.value = res.data?.records || []
  } catch (e) { console.error(e) }
}

const fetchPostTags = async () => {
  try {
    const res = await postTagApi.getList(route.params.postId)
    postTags.value = res.data || []
    selectedTags.value = [...postTags.value]
  } catch (e) { console.error(e) }
}

const fetchAvailableTags = async () => {
  try {
    const res = await tagApi.list({ page: 1, pageSize: 50 })
    availableTags.value = res.data?.records || []
  } catch (e) { console.error(e) }
}

const isTagSelected = (tagId) => {
  return selectedTags.value.some(t => t.tagId === tagId)
}

const toggleTag = (tag) => {
  const tagId = tag.tagId
  const idx = selectedTags.value.findIndex(t => t.tagId === tagId)
  
  if (idx > -1) {
    selectedTags.value.splice(idx, 1)
  } else {
    if (selectedTags.value.length < 5) {
      selectedTags.value.push(tag)
    }
  }
}

const saveTags = async () => {
  const userId = localStorage.getItem('userId')
  if (!userId) return
  
  const postId = post.value.postId
  const userIdNum = parseInt(userId)
  
  try {
    const currentTagIds = postTags.value.map(t => t.tagId)
    const newTagIds = selectedTags.value.map(t => t.tagId)
    
    const tagsToAdd = newTagIds.filter(id => !currentTagIds.includes(id))
    const tagsToRemove = currentTagIds.filter(id => !newTagIds.includes(id))
    
    for (const tagId of tagsToRemove) {
      await postTagApi.delete({ postId, userId: userIdNum, tagId })
    }
    
    for (const tagId of tagsToAdd) {
      await postTagApi.add({ postId, userId: userIdNum, tagIds: [tagId] })
    }
    
    await fetchPostTags()
    showTagEditor.value = false
    alert('保存成功')
  } catch (e) { 
    console.error(e)
    alert('保存失败: ' + (e.response?.data?.msg || e.message))
  }
}

const openTagEditor = async () => {
  showTagEditor.value = true
  await fetchAvailableTags()
  selectedTags.value = [...postTags.value]
}

const goProfile = () => {
  router.push(`/profile/${post.value.userId}`)
}

const handleLike = async () => {
  const userId = localStorage.getItem('userId')
  if (!userId) return router.push('/login')
  try {
    await likeApi.toggle({ postId: post.value.postId, userId: parseInt(userId) })
    fetchDetail()
  } catch (e) { console.error(e) }
}

const handleDelete = async () => {
  if (!confirm('确定删除这条动态吗？')) return
  const userId = localStorage.getItem('userId')
  try {
    await postApi.delete({ postId: post.value.postId, userId: parseInt(userId) })
    router.push('/')
  } catch (e) { console.error(e) }
}

const previewImage = (idx) => {
  previewUrl.value = post.value.images[idx]
  showPreview.value = true
}

const sharePost = () => {
  const url = `${window.location.origin}/post/${post.value.postId}`
  navigator.clipboard?.writeText(url)
  alert('链接已复制')
}

const focusComment = () => {
  commentInput.value?.focus()
}

const submitComment = async () => {
  const userId = localStorage.getItem('userId')
  if (!userId) return router.push('/login')
  if (!commentText.value.trim()) return
  
  try {
    await commentApi.add({ 
      postId: post.value.postId, 
      userId: parseInt(userId), 
      content: commentText.value 
    })
    commentText.value = ''
    fetchComments()
    fetchDetail()
  } catch (e) { console.error(e) }
}

const deleteComment = async (commentId) => {
  if (!confirm('确定删除这条评论吗？')) return
  const userId = localStorage.getItem('userId')
  try {
    await commentApi.delete({ commentId, userId: parseInt(userId) })
    fetchComments()
    fetchDetail()
  } catch (e) { console.error(e) }
}

onMounted(() => {
  fetchDetail()
  fetchComments()
  fetchPostTags()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: #f8f9fa;
  padding-bottom: 70px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-header h2 {
  font-size: 17px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.header-placeholder {
  width: 36px;
}

.detail-content {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}

.post-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  position: relative;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
}

.username:hover {
  color: #1890ff;
}

.time {
  font-size: 12px;
  color: #999;
}

.more-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
}

.action-menu {
  position: absolute;
  top: 40px;
  right: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 10;
}

.menu-item {
  padding: 12px 20px;
  font-size: 14px;
  color: #ff4757;
  cursor: pointer;
}

.menu-item:hover {
  background: #fff5f5;
}

.post-text {
  font-size: 16px;
  line-height: 1.7;
  color: #333;
  margin-bottom: 16px;
  word-wrap: break-word;
}

.post-images {
  display: grid;
  gap: 8px;
  margin-bottom: 16px;
  grid-template-columns: repeat(2, 1fr);
}

.post-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 12px;
  cursor: pointer;
}

.post-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #999;
  padding-bottom: 12px;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 12px;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.tag {
  padding: 4px 10px;
  font-size: 13px;
  color: var(--primary);
  background: rgba(24, 144, 255, 0.1);
  border-radius: 12px;
}

.edit-tags-btn {
  font-size: 13px;
  color: var(--primary);
  cursor: pointer;
  margin-bottom: 12px;
}

.edit-tags-btn:hover {
  text-decoration: underline;
}

.tag-editor-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.tag-editor-panel {
  background: #fff;
  border-radius: 12px;
  width: 90%;
  max-width: 400px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tag-editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.tag-editor-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
}

.tag-selector {
  padding: 16px;
  flex: 1;
  overflow-y: auto;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 6px 14px;
  font-size: 13px;
  color: #666;
  background: #f0f0f0;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag-item:hover {
  background: #e0e0e0;
}

.tag-item.active {
  background: var(--primary);
  color: #fff;
}

.tag-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #999;
  text-align: center;
}

.tag-editor-footer {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

.cancel-btn {
  flex: 1;
  padding: 12px;
  border: 1px solid #e0e0e0;
  background: #fff;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.save-btn {
  flex: 1;
  padding: 12px;
  border: none;
  background: var(--primary);
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.post-actions {
  display: flex;
  justify-content: space-around;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: transparent;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #f5f5f5;
}

.action-btn.active {
  color: #ff4757;
}

.comment-section {
  margin-top: 20px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.comment-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  position: relative;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-username {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
}

.delete-comment {
  position: absolute;
  top: 0;
  right: 0;
  border: none;
  background: transparent;
  color: #999;
  font-size: 12px;
  cursor: pointer;
}

.delete-comment:hover {
  color: #ff4757;
}

.empty-comments {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-comments p {
  margin: 0;
  color: #999;
}

.empty-comments .sub-text {
  font-size: 13px;
  margin-top: 4px;
}

.comment-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: #fff;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.comment-input-bar.focused {
  box-shadow: 0 -4px 20px rgba(102, 126, 234, 0.2);
}

.comment-input-bar input {
  flex: 1;
  padding: 12px 16px;
  border: none;
  background: #f5f5f5;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.comment-input-bar input:focus {
  background: #fff;
  box-shadow: 0 0 0 2px #1890ff;
}

.send-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 20px;
  background: #ccc;
  color: #fff;
  font-size: 14px;
  cursor: not-allowed;
  transition: all 0.3s ease;
}

.send-btn.active {
  background: linear-gradient(135deg, #1890ff, #1890ff);
  cursor: pointer;
}

.send-btn.active:hover {
  transform: scale(1.05);
}

.image-preview {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 40px;
}

.preview-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .detail-content {
    padding: 0;
  }
  
  .post-card {
    border-radius: 0;
  }
  
  .comment-section {
    border-radius: 0;
  }
  
  .post-image {
    height: 160px;
  }
  
  .comment-input-bar {
    padding-bottom: calc(12px + var(--bottom-nav-height));
  }
}
</style>