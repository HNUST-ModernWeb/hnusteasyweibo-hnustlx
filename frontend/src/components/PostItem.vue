<template>
  <div class="post-card">
    <div class="post-header">
      <img 
        :src="post.avatar || defaultAvatar" 
        class="avatar" 
        @click.stop="goProfile"
      />
      <div class="user-info">
        <span class="username" @click.stop="goProfile">{{ post.username }}</span>
        <span class="time">{{ formatTime(post.createTime) }}</span>
      </div>
      <button v-if="isOwner()" class="delete-btn" @click.stop="handleDelete">🗑️</button>
    </div>
    
    <div class="post-content" @click.stop="$emit('click')">{{ post.content }}</div>
    
    <div v-if="post.images && post.images.length" class="post-images" @click.stop>
      <img 
        v-for="(img, idx) in post.images" 
        :key="idx" 
        :src="img" 
        class="post-image"
        @click.stop="previewImage(idx)"
      />
    </div>
    
    <div v-if="postTags.length" class="post-tags" @click.stop>
      <span 
        v-for="tag in postTags" 
        :key="tag.tagId" 
        class="tag"
        @click.stop="goTag(tag.tagId)"
      >
        {{ tag.tagName }}
      </span>
    </div>
    
    <div class="post-actions" @click.stop>
      <button 
        :class="['action-btn', { active: post.isLiked }]" 
        @click.stop="handleLike"
      >
        <span class="icon">{{ post.isLiked ? '❤️' : '🤍' }}</span>
        <span class="count">{{ post.likeCount || 0 }}</span>
      </button>
      
      <button class="action-btn" @click.stop="$emit('click')">
        <span class="icon">💬</span>
        <span class="count">{{ post.commentCount || 0 }}</span>
      </button>
      
      <button class="action-btn" @click.stop="share">
        <span class="icon">🔗</span>
        <span class="count">分享</span>
      </button>
    </div>
    
    <div v-if="showPreview" class="image-preview" @click="showPreview = false">
      <img :src="previewUrl" class="preview-img" @click.stop />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { likeApi, postApi, postTagApi } from '../api'

const props = defineProps({ post: Object })
const emit = defineEmits(['click', 'refresh'])

const router = useRouter()
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
const previewUrl = ref('')
const showPreview = ref(false)
const postTags = ref([])

const fetchPostTags = async () => {
  if (!props.post.postId) return
  try {
    const res = await postTagApi.getList(props.post.postId)
    postTags.value = res.data || []
  } catch (e) {
    console.error('获取标签失败:', e)
  }
}

const isOwner = () => {
  const userId = localStorage.getItem('userId')
  return userId && parseInt(userId) === props.post.userId
}

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

const goProfile = () => {
  router.push(`/profile/${props.post.userId}`)
}

const handleLike = async () => {
  console.log('点赞 clicked, postId:', props.post.postId)
  const userId = localStorage.getItem('userId')
  console.log('userId:', userId)
  if (!userId) {
    alert('请先登录')
    return router.push('/login')
  }
  try {
    const res = await likeApi.toggle({ 
      postId: props.post.postId, 
      userId: parseInt(userId) 
    })
    console.log('点赞结果:', res)
    emit('refresh', true)
  } catch (e) { 
    console.error('点赞失败:', e)
    alert('点赞失败: ' + (e.response?.data?.msg || e.message))
  }
}

const handleDelete = async () => {
  if (!confirm('确定删除这条动态吗？')) return
  const userId = localStorage.getItem('userId')
  try {
    await postApi.delete({ postId: props.post.postId, userId: parseInt(userId) })
    emit('refresh')
  } catch (e) { console.error(e) }
}

const previewImage = (idx) => {
  previewUrl.value = props.post.images[idx]
  showPreview.value = true
}

const share = () => {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(`${window.location.origin}/post/${props.post.postId}`)
    alert('链接已复制')
  }
}

const goTag = (tagId) => {
  console.log('点击标签:', tagId)
}

onMounted(() => {
  fetchPostTags()
})
</script>

<style scoped>
.post-card {
  background: var(--bg-card);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
  transform: translateY(-1px);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f0f0f0;
  transition: all 0.3s ease;
}

.avatar:hover {
  border-color: var(--primary);
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}

.username:hover {
  color: var(--primary);
}

.time {
  font-size: 12px;
  color: #999;
}

.delete-btn {
  padding: 4px 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  opacity: 0.4;
  font-size: 14px;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  opacity: 1;
}

.post-content {
  font-size: 15px;
  line-height: 1.6;
  color: var(--text);
  margin-bottom: 12px;
  word-wrap: break-word;
}

.post-images {
  display: grid;
  gap: 6px;
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;
}

.post-images:has(.post-image:nth-child(1):last-child) {
  grid-template-columns: 1fr;
}

.post-images:has(.post-image:nth-child(2):last-child) {
  grid-template-columns: 1fr 1fr;
}

.post-images:has(.post-image:nth-child(3)) {
  grid-template-columns: 1fr 1fr;
}

.post-images .post-image:nth-child(3) {
  grid-column: span 2;
}

.post-image {
  width: 100%;
  height: 160px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.post-image:hover {
  transform: scale(1.02);
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.tag {
  padding: 4px 10px;
  font-size: 12px;
  color: var(--primary);
  background: rgba(24, 144, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag:hover {
  background: rgba(24, 144, 255, 0.2);
}

.post-actions {
  display: flex;
  gap: 24px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: transparent;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #666;
  font-size: 14px;
}

.action-btn:hover {
  background: #f5f5f5;
  color: var(--primary);
}

.action-btn.active {
  color: #ff4757;
}

.action-btn .icon {
  font-size: 16px;
}

.action-btn .count {
  font-size: 13px;
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

@media (max-width: 600px) {
  .post-card {
    border-radius: 0;
    margin-bottom: 8px;
  }
  
  .post-image {
    height: 150px;
  }
}
</style>